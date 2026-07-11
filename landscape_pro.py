import os
import tkinter as tk
from tkinter import filedialog, messagebox
import customtkinter as ctk
from PIL import Image, ImageTk, ImageOps
import sys

# Import our custom photographic compositing engine
import generator

# Configure window appearance
ctk.set_appearance_mode("dark")
# Emerald green theme matching garden aesthetics
ctk.set_default_color_theme("green")

BASE_DIR = os.path.dirname(os.path.abspath(__file__))
ASSETS_DIR = os.path.join(BASE_DIR, "assets")

# Load environment variables from .env file if it exists
def load_dotenv():
    paths = [
        os.path.join(BASE_DIR, ".env"),
        os.path.expanduser("~/.env")
    ]
    for p in paths:
        if os.path.exists(p):
            try:
                with open(p, "r") as f:
                    for line in f:
                        line = line.strip()
                        if line and not line.startswith("#") and "=" in line:
                            k, v = line.split("=", 1)
                            os.environ[k.strip()] = v.strip().strip("'").strip('"')
            except Exception as e:
                print(f"Error loading .env from {p}: {e}")

load_dotenv()

class CompareCanvas(tk.Canvas):
    """
    Custom Canvas widget that displays two images with a draggable vertical swipe divider.
    """
    def __init__(self, parent, **kwargs):
        super().__init__(parent, highlightthickness=0, cursor="sb_h_double_arrow", **kwargs)
        self.orig_img = None  # Original PIL Image
        self.comp_img = None  # Composted PIL Image
        
        self.scaled_orig = None
        self.scaled_comp = None
        self.tk_image = None
        
        self.divider_x = 0.5  # Normalized divider position (0.0 to 1.0)
        self.is_dragging = False
        
        # Mouse bindings for dragging the divider
        self.bind("<ButtonPress-1>", self.on_press)
        self.bind("<ButtonRelease-1>", self.on_release)
        self.bind("<Motion>", self.on_motion)
        self.bind("<Configure>", self.on_resize)
        
    def set_images(self, orig, comp):
        """
        Updates the images to display.
        """
        self.orig_img = orig
        self.comp_img = comp
        self.render()
        
    def on_press(self, event):
        # Check if click is near the divider line
        w = self.winfo_width()
        click_x = event.x
        div_pixel_x = int(self.divider_x * w)
        
        # Allow a tolerance of 15 pixels
        if abs(click_x - div_pixel_x) < 25:
            self.is_dragging = True
            self.update_divider(click_x)
            
    def on_release(self, event):
        self.is_dragging = False
        
    def on_motion(self, event):
        w = self.winfo_width()
        div_pixel_x = int(self.divider_x * w)
        
        # Update cursor based on hover
        if abs(event.x - div_pixel_x) < 25:
            self.config(cursor="sb_h_double_arrow")
        else:
            self.config(cursor="arrow")
            
        if self.is_dragging:
            self.update_divider(event.x)
            
    def on_resize(self, event):
        self.render()
        
    def update_divider(self, x):
        w = self.winfo_width()
        if w > 0:
            # Clamp divider between 5% and 95% of width
            self.divider_x = max(0.05, min(0.95, x / w))
            self.render()
            
    def render(self):
        if not self.orig_img or not self.comp_img:
            # Draw placeholder text
            self.delete("all")
            w = self.winfo_width()
            h = self.winfo_height()
            self.create_text(w//2, h//2, text="Upload or select a background photo to begin", 
                             fill="#9ca3af", font=("Outfit", 14, "bold"))
            return
            
        # Get current widget dimensions
        canvas_w = self.winfo_width()
        canvas_h = self.winfo_height()
        
        if canvas_w <= 1 or canvas_h <= 1:
            return  # Not yet laid out
            
        # 1. Scale images to fit canvas while maintaining aspect ratio
        img_w, img_h = self.orig_img.size
        ratio_w = canvas_w / img_w
        ratio_h = canvas_h / img_h
        scale = min(ratio_w, ratio_h)
        
        new_w = int(img_w * scale)
        new_h = int(img_h * scale)
        
        # Center coordinates
        offset_x = (canvas_w - new_w) // 2
        offset_y = (canvas_h - new_h) // 2
        
        # Resize PIL images
        scaled_o = self.orig_img.resize((new_w, new_h), Image.Resampling.LANCZOS)
        scaled_c = self.comp_img.resize((new_w, new_h), Image.Resampling.LANCZOS)
        
        # 2. Slice and composite them based on the divider position
        # Calculate local pixel divider coordinate inside the scaled image
        local_div_x = int(self.divider_x * canvas_w) - offset_x
        local_div_x = max(0, min(new_w, local_div_x))
        
        # Create a new blank frame for rendering
        frame = Image.new("RGBA", (new_w, new_h))
        
        if local_div_x > 0:
            left_crop = scaled_o.crop((0, 0, local_div_x, new_h))
            frame.paste(left_crop, (0, 0))
            
        if local_div_x < new_w:
            right_crop = scaled_c.crop((local_div_x, 0, new_w, new_h))
            frame.paste(right_crop, (local_div_x, 0))
            
        # Convert to tk compatible PhotoImage
        self.tk_image = ImageTk.PhotoImage(frame)
        
        # Draw onto canvas
        self.delete("all")
        self.create_image(offset_x, offset_y, anchor="nw", image=self.tk_image)
        
        # 3. Draw vertical divider line and handle
        div_x = offset_x + local_div_x
        # Divider Line
        self.create_line(div_x, offset_y, div_x, offset_y + new_h, fill="#10b981", width=3)
        # Handle Circle
        cy = offset_y + new_h // 2
        self.create_oval(div_x - 18, cy - 18, div_x + 18, cy + 18, fill="#1e293b", outline="#10b981", width=3)
        # Handle Arrows / Detail
        self.create_line(div_x - 8, cy, div_x - 3, cy - 5, fill="#10b981", width=2)
        self.create_line(div_x - 8, cy, div_x - 3, cy + 5, fill="#10b981", width=2)
        self.create_line(div_x + 8, cy, div_x + 3, cy - 5, fill="#10b981", width=2)
        self.create_line(div_x + 8, cy, div_x + 3, cy + 5, fill="#10b981", width=2)
        self.create_line(div_x - 8, cy, div_x + 8, cy, fill="#10b981", width=2)
        
        # 4. Draw labels
        # Before label (left side)
        if div_x > 70:
            self.create_rectangle(offset_x + 10, offset_y + 10, offset_x + 80, offset_y + 35, 
                                 fill="#0f172a", outline="#10b981", width=1)
            self.create_text(offset_x + 45, offset_y + 22, text="BEFORE", fill="#10b981", font=("Outfit", 10, "bold"))
            
        # After label (right side)
        if (offset_x + new_w - div_x) > 70:
            self.create_rectangle(offset_x + new_w - 80, offset_y + 10, offset_x + new_w - 10, offset_y + 35, 
                                 fill="#0f172a", outline="#10b981", width=1)
            self.create_text(offset_x + new_w - 45, offset_y + 22, text="AFTER", fill="#10b981", font=("Outfit", 10, "bold"))


class LandscapeProApp(ctk.CTk):
    def __init__(self):
        super().__init__()
        
        # Window setup
        self.title("LandscapePro AI - Smart Garden Planner")
        self.geometry("1200x820")
        self.minsize(1050, 750)
        
        # State variables
        self.current_orig_image = None  # PIL Image
        self.current_comp_image = None  # PIL Image
        self.generated_concepts = {}    # Caches concepts {1: img, 2: img, 3: img}
        self.gallery_images = []        # Stores dicts {"name": str, "path": str, "pil": PIL.Image}
        self.active_concept_idx = 1
        
        # Setup UI
        self.create_layout()
        self.load_default_gallery()
        
    def create_layout(self):
        # Configure grid grid layout (1 row, 2 columns)
        self.grid_rowconfigure(0, weight=1)
        self.grid_columnconfigure(0, weight=0) # Sidebar
        self.grid_columnconfigure(1, weight=1) # Main View
        
        # ==========================================
        # 1. LEFT SIDEBAR (SCROLLABLE CONTROLS)
        # ==========================================
        self.sidebar = ctk.CTkScrollableFrame(self, width=320, corner_radius=0)
        self.sidebar.grid(row=0, column=0, sticky="nsew", padx=0, pady=0)
        
        # App Title & Branding
        self.title_label = ctk.CTkLabel(self.sidebar, text="LandscapePro AI", font=("Outfit", 24, "bold"), text_color="#10b981")
        self.title_label.pack(pady=(20, 5), padx=15, anchor="w")
        self.subtitle_label = ctk.CTkLabel(self.sidebar, text="Smart Garden Planner", font=("Outfit", 12), text_color="#9ca3af")
        self.subtitle_label.pack(pady=(0, 20), padx=15, anchor="w")
        
        # CONTROL GROUPS (Using CTkFrame for card look)
        
        # CARD 1: LOCATION & USDA CLIMATE
        self.card_loc = ctk.CTkFrame(self.sidebar, fg_color="#1e293b")
        self.card_loc.pack(fill="x", padx=15, pady=10)
        
        self.lbl_loc_title = ctk.CTkLabel(self.card_loc, text="📍 Climate & Hardiness", font=("Outfit", 14, "bold"))
        self.lbl_loc_title.pack(anchor="w", padx=15, pady=(12, 8))
        
        # Zip Input Row
        self.zip_frame = ctk.CTkFrame(self.card_loc, fg_color="transparent")
        self.zip_frame.pack(fill="x", padx=15, pady=5)
        
        self.ent_zip = ctk.CTkEntry(self.zip_frame, placeholder_text="Enter USA Zip Code", width=140)
        self.ent_zip.pack(side="left", fill="x", expand=True, padx=(0, 5))
        self.ent_zip.insert(0, "90210") # Default
        
        self.btn_zip_lookup = ctk.CTkButton(self.zip_frame, text="Lookup", width=70, command=self.lookup_zip)
        self.btn_zip_lookup.pack(side="right")
        
        # Zone result label
        self.lbl_zone_info = ctk.CTkLabel(self.card_loc, text="Zone 9b: Min Temp 25°F to 30°F\nPacific Coastal / Mediterranean", 
                                         font=("Outfit", 12), text_color="#10b981", justify="left")
        self.lbl_zone_info.pack(anchor="w", padx=15, pady=(5, 12))
        
        # CARD 2: ENVIRONMENTAL PARAMETERS
        self.card_env = ctk.CTkFrame(self.sidebar, fg_color="#1e293b")
        self.card_env.pack(fill="x", padx=15, pady=10)
        
        self.lbl_env_title = ctk.CTkLabel(self.card_env, text="🌱 Environmental Factors", font=("Outfit", 14, "bold"))
        self.lbl_env_title.pack(anchor="w", padx=15, pady=(12, 8))
        
        # Soil Type
        self.lbl_soil = ctk.CTkLabel(self.card_env, text="Soil Texture:", font=("Outfit", 12))
        self.lbl_soil.pack(anchor="w", padx=15, pady=(5, 0))
        self.opt_soil = ctk.CTkOptionMenu(self.card_env, values=["Loamy", "Clay", "Sandy", "Silty", "Peaty", "Chalky"])
        self.opt_soil.pack(fill="x", padx=15, pady=(2, 10))
        self.opt_soil.set("Loamy")
        
        # Sun Exposure
        self.lbl_sun = ctk.CTkLabel(self.card_env, text="Sunlight Level:", font=("Outfit", 12))
        self.lbl_sun.pack(anchor="w", padx=15, pady=(5, 0))
        self.opt_sun = ctk.CTkOptionMenu(self.card_env, values=["Full Sun", "Partial Shade", "Full Shade"])
        self.opt_sun.pack(fill="x", padx=15, pady=(2, 10))
        self.opt_sun.set("Full Sun")
        
        # Moisture
        self.lbl_moisture = ctk.CTkLabel(self.card_env, text="Soil Moisture:", font=("Outfit", 12))
        self.lbl_moisture.pack(anchor="w", padx=15, pady=(5, 0))
        self.opt_moisture = ctk.CTkOptionMenu(self.card_env, values=["Well-drained", "Dry", "Moist", "Wet"])
        self.opt_moisture.pack(fill="x", padx=15, pady=(2, 12))
        self.opt_moisture.set("Well-drained")
        
        # CARD 3: DESIGN CONFIGURATION
        self.card_design = ctk.CTkFrame(self.sidebar, fg_color="#1e293b")
        self.card_design.pack(fill="x", padx=15, pady=10)
        
        self.lbl_design_title = ctk.CTkLabel(self.card_design, text="🎨 Garden Design Style", font=("Outfit", 14, "bold"))
        self.lbl_design_title.pack(anchor="w", padx=15, pady=(12, 8))
        
        # Theme
        self.lbl_theme = ctk.CTkLabel(self.card_design, text="Garden Theme:", font=("Outfit", 12))
        self.lbl_theme.pack(anchor="w", padx=15, pady=(5, 0))
        self.opt_theme = ctk.CTkOptionMenu(self.card_design, values=[
            "English Cottage Garden", "Modern Dry Xeriscape", "Japanese Zen Garden", 
            "Wildflower Meadow", "Mediterranean Terrace", "Pacific NW Rain Garden", 
            "Desert Southwest Oasis", "Woodland Shade Glen", "Formal French Parterre", 
            "Tropical Paradise Jungle", "Pollinator Sanctuary", "Rock Garden Alpine"
        ])
        self.opt_theme.pack(fill="x", padx=15, pady=(2, 10))
        self.opt_theme.set("English Cottage Garden")
        
        # Season
        self.lbl_season = ctk.CTkLabel(self.card_design, text="Target Season:", font=("Outfit", 12))
        self.lbl_season.pack(anchor="w", padx=15, pady=(5, 0))
        self.opt_season = ctk.CTkSegmentedButton(self.card_design, values=["Spring", "Summer", "Autumn", "Winter"])
        self.opt_season.pack(fill="x", padx=15, pady=(2, 10))
        self.opt_season.set("Summer")
        
        # Annual vs Perennial Ratio
        self.lbl_ratio = ctk.CTkLabel(self.card_design, text="Composition: 50% Perennials", font=("Outfit", 12))
        self.lbl_ratio.pack(anchor="w", padx=15, pady=(5, 0))
        self.sld_ratio = ctk.CTkSlider(self.card_design, from_=0, to=100, number_of_steps=10, command=self.update_ratio_label)
        self.sld_ratio.pack(fill="x", padx=15, pady=(2, 12))
        self.sld_ratio.set(50)
        
        # CARD 4: OPTIONAL GENERATIVE AI (API CONFIG)
        self.card_ai = ctk.CTkFrame(self.sidebar, fg_color="#1e293b")
        self.card_ai.pack(fill="x", padx=15, pady=10)
        
        self.lbl_ai_title = ctk.CTkLabel(self.card_ai, text="🤖 True Generative AI (Optional)", font=("Outfit", 14, "bold"))
        self.lbl_ai_title.pack(anchor="w", padx=15, pady=(12, 8))
        
        self.lbl_api_key = ctk.CTkLabel(self.card_ai, text="Google Gemini API Key:", font=("Outfit", 12))
        self.lbl_api_key.pack(anchor="w", padx=15, pady=(2, 0))
        
        self.key_entry_frame = ctk.CTkFrame(self.card_ai, fg_color="transparent")
        self.key_entry_frame.pack(fill="x", padx=15, pady=(2, 12))
        
        self.ent_api_key = ctk.CTkEntry(self.key_entry_frame, placeholder_text="Enter API Key here...", show="*")
        self.ent_api_key.pack(side="left", fill="x", expand=True, padx=(0, 5))
        
        self.btn_show_key = ctk.CTkButton(self.key_entry_frame, text="👁️", width=35, command=self.toggle_api_key_visibility)
        self.btn_show_key.pack(side="right")
        
        # Pre-populate API key if it's already in the environment
        env_key = os.environ.get("GEMINI_API_KEY", "")
        print(f"DEBUG: Checking environment for GEMINI_API_KEY... found: {bool(env_key)} (len={len(env_key)})")
        if env_key:
            self.ent_api_key.insert(0, env_key)
        
        # GENERATE BUTTON (Main Action)
        self.btn_generate = ctk.CTkButton(self.sidebar, text="Generate AI Landscape", font=("Outfit", 16, "bold"), 
                                         height=45, fg_color="#10b981", hover_color="#059669", command=self.generate_landscape)
        self.btn_generate.pack(fill="x", padx=15, pady=(15, 20))
        
        # ==========================================
        # 2. RIGHT DISPLAY PANEL
        # ==========================================
        self.main_view = ctk.CTkFrame(self, fg_color="#0f172a")
        self.main_view.grid(row=0, column=1, sticky="nsew", padx=0, pady=0)
        
        self.main_view.grid_rowconfigure(0, weight=0) # Tabs/Header
        self.main_view.grid_rowconfigure(1, weight=1) # Interactive Canvas
        self.main_view.grid_rowconfigure(2, weight=0) # Bottom Gallery & Exports
        self.main_view.grid_columnconfigure(0, weight=1)
        
        # Top Header (Segmented Button for Concept selection)
        self.header_frame = ctk.CTkFrame(self.main_view, fg_color="transparent")
        self.header_frame.grid(row=0, column=0, sticky="ew", padx=20, pady=(15, 5))
        
        self.concept_selector = ctk.CTkSegmentedButton(self.header_frame, 
                                                      values=["Concept 1 (Standard)", "Concept 2 (Lush)", "Concept 3 (Minimalist)"],
                                                      command=self.on_concept_changed)
        self.concept_selector.pack(side="left")
        self.concept_selector.set("Concept 1 (Standard)")
        
        # Export Actions Frame
        self.export_frame = ctk.CTkFrame(self.header_frame, fg_color="transparent")
        self.export_frame.pack(side="right")
        
        self.opt_export_format = ctk.CTkOptionMenu(self.export_frame, values=["PNG", "JPEG", "WEBP"], width=80)
        self.opt_export_format.pack(side="left", padx=5)
        self.opt_export_format.set("PNG")
        
        self.btn_export = ctk.CTkButton(self.export_frame, text="💾 Export Design", width=120, command=self.export_design)
        self.btn_export.pack(side="right")
        
        # Workspace Compare Canvas
        self.workspace_canvas = CompareCanvas(self.main_view, bg="#1e293b")
        self.workspace_canvas.grid(row=1, column=0, sticky="nsew", padx=20, pady=10)
        
        # Bottom Gallery Frame
        self.bottom_gallery = ctk.CTkFrame(self.main_view, fg_color="#1e293b", height=130)
        self.bottom_gallery.grid(row=2, column=0, sticky="ew", padx=20, pady=(5, 20))
        self.bottom_gallery.pack_propagate(False)
        
        self.lbl_gallery_title = ctk.CTkLabel(self.bottom_gallery, text="📸 Background Photo Library", font=("Outfit", 12, "bold"))
        self.lbl_gallery_title.pack(anchor="w", padx=15, pady=(5, 2))
        
        # Container for gallery items (horizontal scrolling frame)
        self.items_container = ctk.CTkFrame(self.bottom_gallery, fg_color="transparent")
        self.items_container.pack(fill="both", expand=True, padx=15, pady=(0, 5))
        
        self.btn_upload = ctk.CTkButton(self.items_container, text="➕\nUpload Photo", width=100, height=80,
                                       fg_color="#334155", hover_color="#475569", font=("Outfit", 12), command=self.upload_custom_photo)
        self.btn_upload.pack(side="left", padx=(0, 10))
        
        self.gallery_item_buttons = []
        
    def toggle_api_key_visibility(self):
        if self.ent_api_key.cget("show") == "*":
            self.ent_api_key.configure(show="")
            self.btn_show_key.configure(text="🙈")
        else:
            self.ent_api_key.configure(show="*")
            self.btn_show_key.configure(text="👁️")
            
    def load_default_gallery(self):
        """
        Loads the pre-packaged backgrounds into the gallery list.
        """
        defaults = [
            {"name": "Bare Yard", "filename": "template_bare.png"},
            {"name": "Muddy Yard", "filename": "google_photo_mud.png"},
            {"name": "Shady Glen", "filename": "google_photo_shade.png"}
        ]
        
        for d in defaults:
            p = os.path.join(ASSETS_DIR, d["filename"])
            if os.path.exists(p):
                try:
                    img = Image.open(p).convert("RGBA")
                    self.gallery_images.append({
                        "name": d["name"],
                        "path": p,
                        "pil": img
                    })
                except Exception as e:
                    print(f"Error pre-loading gallery image {d['filename']}: {e}")
                    
        self.refresh_gallery_ui()
        
        # Select first image by default
        if self.gallery_images:
            self.select_gallery_item(0)
            
    def refresh_gallery_ui(self):
        # Clear existing buttons (except the upload button)
        for btn in self.gallery_item_buttons:
            btn.destroy()
        self.gallery_item_buttons = []
        
        # Add a button for each image in our list
        for idx, item in enumerate(self.gallery_images):
            # Create a thumbnail
            thumb = item["pil"].copy()
            thumb.thumbnail((90, 60), Image.Resampling.LANCZOS)
            tk_thumb = ImageTk.PhotoImage(thumb)
            
            # Using standard Button inside Tkinter container for custom photo image preview
            # but wrap in frame to match CustomTkinter rounded borders
            f = ctk.CTkFrame(self.items_container, width=100, height=80, fg_color="#334155")
            f.pack(side="left", padx=5)
            f.pack_propagate(False)
            
            btn = tk.Button(f, image=tk_thumb, borderwidth=0, bg="#334155", activebackground="#10b981",
                            command=lambda i=idx: self.select_gallery_item(i))
            btn.image = tk_thumb  # keep reference
            btn.pack(fill="both", expand=True, padx=2, pady=2)
            
            lbl = ctk.CTkLabel(f, text=item["name"], font=("Outfit", 9), fg_color="#0f172a", text_color="#9ca3af", height=15)
            lbl.pack(fill="x", side="bottom")
            
            self.gallery_item_buttons.append(f)
            
    def upload_custom_photo(self):
        paths = filedialog.askopenfilenames(
            title="Select Garden Space Photos",
            filetypes=[("Image Files", "*.png *.jpg *.jpeg *.webp *.bmp")]
        )
        if not paths:
            return
            
        for path in paths:
            try:
                img = Image.open(path).convert("RGBA")
                name = os.path.basename(path)
                if len(name) > 15:
                    name = name[:12] + "..."
                self.gallery_images.append({
                    "name": name,
                    "path": path,
                    "pil": img
                })
            except Exception as e:
                messagebox.showerror("Error", f"Failed to load image {os.path.basename(path)}:\n{e}")
                
        self.refresh_gallery_ui()
        # Select the last uploaded image
        self.select_gallery_item(len(self.gallery_images) - 1)
        
    def select_gallery_item(self, idx):
        if idx < 0 or idx >= len(self.gallery_images):
            return
            
        item = self.gallery_images[idx]
        self.current_orig_image = item["pil"]
        
        # Clear generated concept cache
        self.generated_concepts.clear()
        
        # Automatically run generation for the newly selected image
        self.generate_landscape()
        
    def lookup_zip(self):
        zip_str = self.ent_zip.get().strip()
        if not zip_str:
            messagebox.showwarning("Warning", "Please enter a valid USA zip code.")
            return
            
        # Get USDA Hardiness Zone details
        zone_info = self.get_zone_for_zip(zip_str)
        self.lbl_zone_info.configure(text=f"Zone {zone_info['zone']}: {zone_info['min_temp']}\n{zone_info['climate']}", text_color="#10b981")
        
    def get_zone_for_zip(self, zip_str):
        # Match original app's zone fallback logic based on first digit of zip
        first_digit = zip_str[0] if len(zip_str) > 0 else '7'
        mappings = {
            '0': {'zone': '6b', 'min_temp': 'Min Temp -5°F to 0°F', 'climate': 'Northeastern Coastal/Temperate'},
            '1': {'zone': '7a', 'min_temp': 'Min Temp 0°F to 5°F', 'climate': 'Mid-Atlantic Temperate'},
            '2': {'zone': '7b', 'min_temp': 'Min Temp 5°F to 10°F', 'climate': 'Upper South Temperate'},
            '3': {'zone': '9a', 'min_temp': 'Min Temp 20°F to 25°F', 'climate': 'Humid Subtropical'},
            '4': {'zone': '6a', 'min_temp': 'Min Temp -10°F to -5°F', 'climate': 'East Central Continental'},
            '5': {'zone': '4b', 'min_temp': 'Min Temp -25°F to -20°F', 'climate': 'Upper Midwest Continental'},
            '6': {'zone': '6a', 'min_temp': 'Min Temp -10°F to -5°F', 'climate': 'Midwest Continental'},
            '7': {'zone': '8a', 'min_temp': 'Min Temp 10°F to 15°F', 'climate': 'South Central Subtropical'},
            '8': {'zone': '7a', 'min_temp': 'Min Temp 0°F to 5°F', 'climate': 'Intermountain / Semi-Arid'},
            '9': {'zone': '9b', 'min_temp': 'Min Temp 25°F to 30°F', 'climate': 'Pacific Coastal / Mediterranean'},
        }
        return mappings.get(first_digit, {'zone': '7b', 'min_temp': 'Min Temp 5°F to 10°F', 'climate': 'Temperate Climate'})
        
    def update_ratio_label(self, val):
        pct_perennial = int(val)
        self.lbl_ratio.configure(text=f"Composition: {pct_perennial}% Perennials / {100 - pct_perennial}% Annuals")
        
    def on_concept_changed(self, val):
        if "Concept 1" in val:
            self.active_concept_idx = 1
        elif "Concept 2" in val:
            self.active_concept_idx = 2
        else:
            self.active_concept_idx = 3
            
        # Display from cache if exists, otherwise generate
        if self.active_concept_idx in self.generated_concepts:
            self.current_comp_image = self.generated_concepts[self.active_concept_idx]
            self.workspace_canvas.set_images(self.current_orig_image, self.current_comp_image)
        else:
            self.generate_landscape()
            
    def generate_landscape(self):
        if not self.current_orig_image:
            return
            
        self.btn_generate.configure(state="disabled", text="Generating...")
        self.update() # Update UI
        
        # Load parameters
        theme_map = {
            "English Cottage Garden": "cottage",
            "Modern Dry Xeriscape": "xeriscape",
            "Japanese Zen Garden": "zen",
            "Wildflower Meadow": "meadow",
            "Mediterranean Terrace": "mediterranean",
            "Pacific NW Rain Garden": "rain-garden",
            "Desert Southwest Oasis": "desert-oasis",
            "Woodland Shade Glen": "woodland-shade",
            "Formal French Parterre": "formal-french",
            "Tropical Paradise Jungle": "tropical-jungle",
            "Pollinator Sanctuary": "pollinator",
            "Rock Garden Alpine": "rock-alpine"
        }
        
        theme_key = theme_map.get(self.opt_theme.get(), "cottage")
        soil = self.opt_soil.get().lower()
        sun = self.opt_sun.get().lower().replace(" ", "-")
        water = self.opt_moisture.get().lower().replace(" ", "-")
        ratio = self.sld_ratio.get() / 100.0
        season = self.opt_season.get().lower()
        
        # Check if Google Gemini API Key is provided for True Generative AI
        api_key = self.ent_api_key.get().strip()
        
        if api_key:
            # TRUE GENERATIVE AI ROUTE
            # Create a rich description prompt
            prompt_theme = self.opt_theme.get()
            prompt_season = self.opt_season.get()
            prompt_sun = self.opt_sun.get()
            
            # Extract USDA Zone details for the prompt
            zip_str = self.ent_zip.get().strip()
            zone_info = self.get_zone_for_zip(zip_str)
            
            prompt = (
                f"A professional landscape design photo of a backyard garden, {prompt_theme} style, "
                f"during {prompt_season} season, under {prompt_sun} exposure. "
                f"Suitable for USDA Hardiness Zone {zone_info['zone']}. The landscaping features a "
                f"clean mulched planting bed filled with beautiful, healthy matching plant species, "
                f"neat borders, highly realistic, high resolution, award-winning garden layout."
            )
            
            # Generate via REST API
            ai_img = generator.generate_ai_image_with_gemini(prompt, api_key)
            if ai_img:
                self.current_comp_image = ai_img
                self.generated_concepts[self.active_concept_idx] = ai_img
            else:
                # Fallback to local rendering and alert user
                messagebox.showwarning("AI Generation Failed", "Gemini API generation failed. Falling back to offline photographic overlay engine.")
                self.current_comp_image = generator.generate_landscape(
                    self.current_orig_image, theme_key, soil, "neutral", sun, water, ratio, season, self.active_concept_idx
                )
                self.generated_concepts[self.active_concept_idx] = self.current_comp_image
        else:
            # LOCAL COMPOSITING ENGINE
            # Find the path of the original image if it's a file, or pass image itself.
            # In generator.py, we handle passing PIL Image objects by slightly updating generator to support it,
            # or saving a temp file. Let's make sure generator can receive a PIL Image or path.
            # Let's verify generator.py: it does `bg = Image.open(bg_image_path)`.
            # To handle both PIL Image objects and paths, let's write a small temporary file if needed, or update generator.
            # Actually, let's save the current background as a temp file to avoid changing generator imports,
            # or just write generator to accept a PIL image.
            # Wait, let's check generator.py lines 152: `try: bg = Image.open(bg_image_path) ...`.
            # If bg_image_path is already an Image, or we check isinstance, let's handle it!
            # Let's save a temporary background file in assets or scratch to be 100% safe.
            # Saving to scratch is extremely clean.
            scratch_bg = os.path.join(BASE_DIR, "assets", "temp_bg.png")
            try:
                self.current_orig_image.save(scratch_bg, "PNG")
                self.current_comp_image = generator.generate_landscape(
                    scratch_bg, theme_key, soil, "neutral", sun, water, ratio, season, self.active_concept_idx
                )
                self.generated_concepts[self.active_concept_idx] = self.current_comp_image
            except Exception as e:
                messagebox.showerror("Generation Error", f"Failed local rendering:\n{e}")
                
        # Update Canvas
        self.workspace_canvas.set_images(self.current_orig_image, self.current_comp_image)
        
        self.btn_generate.configure(state="normal", text="Generate AI Landscape")
        
    def export_design(self):
        if not self.current_comp_image:
            messagebox.showwarning("Warning", "No generated design is available to export.")
            return
            
        fmt = self.opt_export_format.get().lower()
        ext_map = {"png": ".png", "jpeg": ".jpg", "webp": ".webp"}
        ext = ext_map.get(fmt, ".png")
        
        # Open Save As dialog
        save_path = filedialog.asksaveasfilename(
            title="Export Landscaped Concept",
            defaultextension=ext,
            filetypes=[(f"{fmt.upper()} Image", f"*{ext}")]
        )
        if not save_path:
            return
            
        try:
            # We want to export the current split view OR the full concept.
            # Let's ask the user if they want to export the Full Concept or the Before/After split.
            choice = messagebox.askyesnocancel(
                "Export View Options",
                "Export the FULL landscaped design (Yes) or the Before/After SPLIT swipe view (No)?"
            )
            
            if choice is None: # Cancel
                return
            elif choice is True: # Full Concept
                self.current_comp_image.convert("RGB").save(save_path, fmt.upper())
            else: # Before/After Split view as displayed on Canvas
                # Capture the current combined image at high resolution
                w, h = self.current_orig_image.size
                div_pixel_x = int(self.workspace_canvas.divider_x * w)
                
                frame = Image.new("RGBA", (w, h))
                if div_pixel_x > 0:
                    left_crop = self.current_orig_image.crop((0, 0, div_pixel_x, h))
                    frame.paste(left_crop, (0, 0))
                if div_pixel_x < w:
                    right_crop = self.current_comp_image.crop((div_pixel_x, 0, w, h))
                    frame.paste(right_crop, (div_pixel_x, 0))
                    
                # Draw vertical divider line in the exported split view
                draw = ImageDraw.Draw(frame)
                draw.line([(div_pixel_x, 0), (div_pixel_x, h)], fill=(16, 185, 129, 255), width=6)
                
                frame.convert("RGB").save(save_path, fmt.upper())
                
            messagebox.showinfo("Export Success", f"Successfully exported design to:\n{save_path}")
        except Exception as e:
            messagebox.showerror("Export Error", f"Failed to export design:\n{e}")

def main():
    # Run the application
    app = LandscapeProApp()
    app.mainloop()

if __name__ == "__main__":
    main()
