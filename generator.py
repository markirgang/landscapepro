import os
import random
import math
from PIL import Image, ImageDraw, ImageFilter, ImageOps, ImageEnhance

# Path configurations
BASE_DIR = os.path.dirname(os.path.abspath(__file__))
PLANTS_DIR = os.path.join(BASE_DIR, "assets", "plants")

# Plant specifications and compatibility
# Mapping of plant keys to file names, common names, and growth requirements
PLANT_DB = {
    "lavender": {
        "file": "lavender.png",
        "name": "English Lavender",
        "sun": ["full-sun"],
        "water": ["dry", "well-drained"],
        "themes": ["cottage", "meadow", "mediterranean", "pollinator"],
        "type": "midground",
        "evergreen": False
    },
    "boxwood": {
        "file": "boxwood.png",
        "name": "Green Velvet Boxwood",
        "sun": ["full-sun", "partial-shade"],
        "water": ["moist", "well-drained"],
        "themes": ["formal-french", "zen", "mediterranean", "woodland-shade"],
        "type": "midground",
        "evergreen": True
    },
    "hosta": {
        "file": "hosta.png",
        "name": "Variegated Hosta",
        "sun": ["partial-shade", "full-shade"],
        "water": ["moist", "wet"],
        "themes": ["woodland-shade", "rain-garden", "tropical-jungle"],
        "type": "foreground",
        "evergreen": False
    },
    "hydrangea": {
        "file": "hydrangea.png",
        "name": "Blue Hydrangea",
        "sun": ["partial-shade"],
        "water": ["moist", "wet"],
        "themes": ["cottage", "rain-garden", "pollinator"],
        "type": "background",
        "evergreen": False
    },
    "agave": {
        "file": "agave.png",
        "name": "Blue Agave",
        "sun": ["full-sun"],
        "water": ["dry"],
        "themes": ["xeriscape", "desert-oasis"],
        "type": "foreground",
        "evergreen": True
    },
    "fern": {
        "file": "fern.png",
        "name": "Ostrich Fern",
        "sun": ["partial-shade", "full-shade"],
        "water": ["moist", "wet"],
        "themes": ["woodland-shade", "rain-garden", "tropical-jungle", "zen"],
        "type": "midground",
        "evergreen": False
    },
    "grass": {
        "file": "grass.png",
        "name": "Feather Reed Grass",
        "sun": ["full-sun", "partial-shade"],
        "water": ["dry", "moist", "well-drained"],
        "themes": ["meadow", "xeriscape", "desert-oasis", "mediterranean", "rock-alpine"],
        "type": "background",
        "evergreen": False
    },
    "rose_bush": {
        "file": "rose_bush.png",
        "name": "Red Shrub Rose",
        "sun": ["full-sun"],
        "water": ["moist", "well-drained"],
        "themes": ["cottage", "formal-french", "pollinator"],
        "type": "background",
        "evergreen": False
    },
    "stone": {
        "file": "stone.png",
        "name": "Mossy Garden Stone",
        "sun": ["full-sun", "partial-shade", "full-shade"],
        "water": ["dry", "moist", "wet", "well-drained"],
        "themes": ["zen", "rock-alpine", "xeriscape", "desert-oasis"],
        "type": "foreground",
        "evergreen": True  # Always present
    }
}

def apply_season_tint(img, season):
    """
    Applies color grading and enhancements to a plant image to reflect the target season.
    """
    if season == "spring":
        # Boost green slightly and make it look fresh
        r, g, b, a = img.split()
        enhancer_sat = ImageEnhance.Color(img)
        img = enhancer_sat.enhance(1.2)
        enhancer_bright = ImageEnhance.Brightness(img)
        img = enhancer_bright.enhance(1.05)
    elif season == "summer":
        # Normal rich colors, no changes needed
        pass
    elif season == "autumn":
        # Tint toward warm orange/yellow/red tones
        r, g, b, a = img.split()
        r_new = r.point(lambda i: min(255, int(i * 1.15)))
        g_new = g.point(lambda i: int(i * 0.85))
        b_new = b.point(lambda i: int(i * 0.70))
        img = Image.merge("RGBA", (r_new, g_new, b_new, a))
        enhancer_sat = ImageEnhance.Color(img)
        img = enhancer_sat.enhance(0.9)
    elif season == "winter":
        # Desaturate strongly, tint blue/cool, and add a white frost overlay
        r, g, b, a = img.split()
        # Desaturate
        enhancer_sat = ImageEnhance.Color(img)
        img = enhancer_sat.enhance(0.4)
        # Apply a cool tint
        r, g, b, a = img.split()
        r_new = r.point(lambda i: int(i * 0.85))
        g_new = g.point(lambda i: int(i * 0.95))
        b_new = b.point(lambda i: min(255, int(i * 1.15)))
        img = Image.merge("RGBA", (r_new, g_new, b_new, a))
        # Add a light white frost top-down overlay
        frost = Image.new("RGBA", img.size, (245, 248, 255, 0))
        # Draw some soft white frost on top-facing parts
        for y in range(img.size[1]):
            opacity = int(70 * (1.0 - (y / img.size[1])))
            if opacity > 0:
                for x in range(img.size[0]):
                    # Only apply to opaque pixels
                    _, _, _, px_a = img.getpixel((x, y))
                    if px_a > 10:
                        frost.putpixel((x, y), (245, 248, 255, opacity))
        img = Image.alpha_composite(img, frost)
        
    return img

def create_drop_shadow(img, offset=(8, 10), blur_radius=12, opacity=100):
    """
    Creates a soft drop shadow image from the plant's alpha mask.
    """
    alpha = img.split()[3]
    shadow = Image.new("RGBA", img.size, (15, 12, 10, 0))
    shadow_mask = alpha.point(lambda x: int(x * (opacity / 255.0)))
    shadow.putalpha(shadow_mask)
    shadow = shadow.filter(ImageFilter.GaussianBlur(blur_radius))
    return shadow

def generate_landscape(bg_image_path, theme, soil, acidity, sun, water, ratio, season, concept_index=1):
    """
    Generates a beautifully blended photo-composite design.
    Returns: A PIL Image representing the final landscaped view.
    """
    print(f"Generating landscape concept {concept_index}...")
    
    # Load background image
    if isinstance(bg_image_path, Image.Image):
        bg = bg_image_path.convert("RGBA")
    else:
        try:
            bg = Image.open(bg_image_path).convert("RGBA")
        except Exception as e:
            print(f"Error loading background {bg_image_path}: {e}")
            # Fallback to a bare template
            fallback_path = os.path.join(BASE_DIR, "assets", "template_bare.png")
            bg = Image.open(fallback_path).convert("RGBA")
        
    w, h = bg.size
    ground_level = int(h * 0.65)
    
    # Initialize random seed for repeatability per concept
    rand = random.Random(concept_index * 42)
    
    # 1. Determine Garden Bed Texture (Mulch vs Gravel)
    is_gravel = theme in ["xeriscape", "zen", "desert-oasis", "rock-alpine"]
    texture_filename = "gravel.jpg" if is_gravel else "mulch.jpg"
    texture_path = os.path.join(PLANTS_DIR, texture_filename)
    
    try:
        texture_img = Image.open(texture_path).convert("RGBA")
    except Exception as e:
        print(f"Error loading texture {texture_filename}: {e}")
        color = (195, 195, 190, 255) if is_gravel else (65, 30, 15, 255)
        texture_img = Image.new("RGBA", (256, 256), color=color)
        
    # 2. Draw the Wavy, Textured Garden Bed
    bed_layer = Image.new("RGBA", (w, h), (0, 0, 0, 0))
    
    # Define an organic wavy top border for the bed
    points = []
    points.append((-50, h + 50))
    
    left_height = ground_level + rand.randint(-20, 20)
    points.append((-50, left_height))
    
    steps = 8
    for i in range(steps + 1):
        x = int(w * (i / steps))
        wave = math.sin(i * 1.5) * 35 + math.cos(i * 3.0) * 15
        y = int(ground_level + wave + rand.randint(-15, 15))
        y = max(int(h * 0.55), min(int(h * 0.82), y))
        points.append((x, y))
        
    points.append((w + 50, ground_level + rand.randint(-20, 20)))
    points.append((w + 50, h + 50))
    
    # Draw bed polygon filled with solid white as mask
    mask = Image.new("L", (w, h), 0)
    mask_draw = ImageDraw.Draw(mask)
    mask_draw.polygon(points, fill=255)
    
    # Create tiled texture to cover the screen
    tiled_texture = Image.new("RGBA", (w, h))
    tex_w, tex_h = texture_img.size
    for tx in range(0, w, tex_w):
        for ty in range(0, h, tex_h):
            tiled_texture.paste(texture_img, (tx, ty))
            
    # Apply soft/feathered edges to the top of the bed mask so it blends with the background grass
    mask_blurred = mask.filter(ImageFilter.GaussianBlur(radius=8))
    bed_layer.paste(tiled_texture, (0, 0), mask=mask_blurred)
    
    if not is_gravel:
        enhancer = ImageEnhance.Brightness(bed_layer)
        if water == "wet" or water == "moist":
            bed_layer = enhancer.enhance(0.7)
        elif water == "dry":
            bed_layer = enhancer.enhance(1.1)
    
    # Composite the garden bed onto the background
    composite = Image.alpha_composite(bg, bed_layer)
    
    # 3. Filter/select plant database based on preferences
    compatible_plants = []
    
    for key, spec in PLANT_DB.items():
        score = 0
        if theme in spec["themes"]:
            score += 5
        if sun in spec["sun"]:
            score += 3
        elif "partial-shade" in spec["sun"] and (sun == "full-sun" or sun == "full-shade"):
            score += 1
        if water in spec["water"] or "well-drained" in spec["water"]:
            score += 2
        if key == "stone":
            score += 3
        compatible_plants.append((key, score))
        
    compatible_plants.sort(key=lambda x: x[1], reverse=True)
    best_plants = [p[0] for p in compatible_plants if p[1] > 0]
    
    if not best_plants:
        best_plants = list(PLANT_DB.keys())
        
    # 4. Generate Plant Positions
    plant_count = 10
    if concept_index == 2:  # Lush
        plant_count = 18
    elif concept_index == 3:  # Minimalist
        plant_count = 6
        
    plants = []
    attempts = 0
    while len(plants) < plant_count and attempts < 200:
        attempts += 1
        px = rand.randint(int(w * 0.02), int(w * 0.98))
        py = rand.randint(int(ground_level - 30), int(h - 10))
        
        if px >= 0 and px < w and py >= 0 and py < h:
            mask_val = mask.getpixel((px, py))
            if mask_val > 100:
                depth_ratio = (py - ground_level) / (h - ground_level + 1)
                if depth_ratio < 0.25:
                    p_type = "background"
                elif depth_ratio > 0.65:
                    p_type = "foreground"
                else:
                    p_type = "midground"
                plants.append((px, py, p_type))
                
    plants.sort(key=lambda p: p[1])
    
    # 5. Composite and Render Plants
    for px, py, p_type in plants:
        type_matching_plants = [k for k in best_plants if PLANT_DB[k]["type"] == p_type]
        if not type_matching_plants:
            type_matching_plants = best_plants
            
        plant_key = rand.choice(type_matching_plants)
        
        if season == "winter" and not PLANT_DB[plant_key]["evergreen"]:
            if rand.random() > 0.5:
                plant_key = "stone"
                
        plant_spec = PLANT_DB[plant_key]
        plant_file = os.path.join(PLANTS_DIR, plant_spec["file"])
        
        if not os.path.exists(plant_file):
            continue
            
        try:
            plant_img = Image.open(plant_file).convert("RGBA")
        except Exception as e:
            print(f"Error loading plant asset {plant_spec['file']}: {e}")
            continue
            
        scale_factor = 0.35 + (0.55 * (py - ground_level) / (h - ground_level + 1))
        scale_factor *= rand.uniform(0.7, 1.2)
        
        if plant_key == "grass" or plant_key == "hydrangea" or plant_key == "rose_bush":
            scale_factor *= 1.1
        elif plant_key == "agave" or plant_key == "stone":
            scale_factor *= 0.85
            
        pw = int(plant_img.size[0] * scale_factor)
        ph = int(plant_img.size[1] * scale_factor)
        
        if pw <= 0 or ph <= 0:
            continue
            
        plant_img = plant_img.resize((pw, ph), Image.Resampling.LANCZOS)
        
        if rand.random() > 0.5:
            plant_img = ImageOps.mirror(plant_img)
            
        plant_img = apply_season_tint(plant_img, season)
        
        shadow_offset_x = int(pw * 0.08)
        shadow_offset_y = int(ph * 0.05)
        shadow_blur = int(min(15, max(3, pw * 0.04)))
        
        sx = px - pw // 2 + shadow_offset_x
        sy = py - ph + shadow_offset_y + int(ph * 0.05)
        
        shadow = create_drop_shadow(plant_img, (shadow_offset_x, shadow_offset_y), shadow_blur, opacity=120)
        composite.paste(shadow, (sx, sy), mask=shadow.split()[3])
        
        rx = px - pw // 2
        ry = py - ph + int(ph * 0.05)
        
        composite.paste(plant_img, (rx, ry), mask=plant_img.split()[3])
        
    return composite

def generate_ai_image_with_gemini(prompt, api_key, base_image=None):
    """
    Calls the Google Gemini REST API to generate or edit a landscaping photo.
    Returns: A PIL Image or None if failed.
    """
    import urllib.request
    import urllib.error
    import json
    import base64
    from io import BytesIO
    
    if base_image:
        # Image-to-Image / Inpainting mode using Gemini 3.1 Flash Image
        try:
            # base_image can be a PIL Image object or a string path
            if isinstance(base_image, str):
                base_img_obj = Image.open(base_image)
            else:
                base_img_obj = base_image
            
            # Ensure it is in RGB format for JPEG saving
            base_img_obj = base_img_obj.convert("RGB")
            
            # Save to buffer
            buffered = BytesIO()
            base_img_obj.save(buffered, format="JPEG")
            img_b64 = base64.b64encode(buffered.getvalue()).decode("utf-8")
            
            url = f"https://generativelanguage.googleapis.com/v1beta/models/gemini-3.1-flash-image:generateContent?key={api_key}"
            
            headers = {
                "Content-Type": "application/json"
            }
            
            data = {
                "contents": [
                    {
                        "parts": [
                            {
                                "inlineData": {
                                    "mimeType": "image/jpeg",
                                    "data": img_b64
                                }
                            },
                            {
                                "text": prompt
                            }
                        ]
                    }
                ],
                "generationConfig": {
                    "responseModalities": ["IMAGE"]
                }
            }
            
            req = urllib.request.Request(
                url,
                data=json.dumps(data).encode("utf-8"),
                headers=headers,
                method="POST"
            )
            
            print(f"Calling Gemini API (generateContent) for image-to-image with prompt: {prompt}...")
            import ssl
            context = ssl._create_unverified_context()
            with urllib.request.urlopen(req, timeout=45, context=context) as response:
                res_data = json.loads(response.read().decode("utf-8"))
                candidates = res_data.get("candidates", [])
                if candidates:
                    parts = candidates[0].get("content", {}).get("parts", [])
                    for part in parts:
                        data_obj = part.get("inlineData") or part.get("inline_data")
                        if data_obj:
                            out_b64 = data_obj.get("data")
                            out_bytes = base64.b64decode(out_b64)
                            print("  Successfully received image from Gemini API.")
                            return Image.open(BytesIO(out_bytes)).convert("RGBA")
                print("  No image returned in Gemini API response.")
                return None
        except Exception as e:
            print(f"Gemini AI Image-to-Image Generation failed: {e}")
            return None
    else:
        # Fallback to Text-to-Image (Imagen 4)
        url = f"https://generativelanguage.googleapis.com/v1beta/models/imagen-4.0-generate-001:predict?key={api_key}"
        
        headers = {
            "Content-Type": "application/json"
        }
        
        data = {
            "instances": [
                {
                    "prompt": prompt
                }
            ],
            "parameters": {
                "sampleCount": 1,
                "outputMimeType": "image/jpeg",
                "aspectRatio": "4:3"
            }
        }
        
        req = urllib.request.Request(
            url,
            data=json.dumps(data).encode("utf-8"),
            headers=headers,
            method="POST"
        )
        
        try:
            print(f"Calling Gemini API (predict) with prompt: {prompt}...")
            import ssl
            context = ssl._create_unverified_context()
            with urllib.request.urlopen(req, timeout=45, context=context) as response:
                res_data = json.loads(response.read().decode("utf-8"))
                img_b64 = res_data["predictions"][0]["bytesBase64Encoded"]
                img_bytes = base64.b64decode(img_b64)
                print("  Successfully received image from Gemini API.")
                return Image.open(BytesIO(img_bytes)).convert("RGBA")
        except Exception as e:
            print(f"Gemini AI Image Generation failed: {e}")
            return None

if __name__ == "__main__":
    print("Testing generator.py...")
    assets_test_bg = os.path.join(BASE_DIR, "assets", "template_bare.png")
    if os.path.exists(assets_test_bg):
        test_out = generate_landscape(
            assets_test_bg,
            theme="cottage",
            soil="loamy",
            acidity="neutral",
            sun="full-sun",
            water="moist",
            ratio=0.5,
            season="autumn"
        )
        test_out.save(os.path.join(BASE_DIR, "test_render_autumn.png"))
        print("Success! Generated test render test_render_autumn.png")
    else:
        print(f"Test background not found at {assets_test_bg}")
