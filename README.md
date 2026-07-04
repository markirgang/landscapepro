# LandscapePro AI - Smart Garden Planner

LandscapePro AI is a premium, client-side web browser application designed to help homeowners and landscape architects visualize garden spaces transformed with AI-generated plants. 

The application is completely self-contained, lightweight, and runs directly in any modern web browser without requiring a backend server or complex build tools.

---

## Key Features

1. **Before/After Split Swipe Slider**: Drag the slider handle to interactively swipe between the original, empty space and the AI-landscaped concept.
2. **Climate-Smart Planting**:
   - Enter/select a USA Zip Code.
   - The app automatically determines the **USDA Hardiness Zone** (e.g. Zone 10a, Zone 6a, Zone 11a).
   - The AI overlay engine automatically adjusts plant selections based on the zone.
3. **Environmental Parameters**:
   - **Soil Type**: Clay, Sandy, Loamy, Silty, Peaty, Chalky.
   - **Sun Exposure**: Full Sun, Partial Shade, Full Shade.
   - **Soil Moisture**: Dry, Moist, Wet, Well-drained.
   - **Plant Composition**: Slider to adjust Perennials vs. Annuals ratio.
4. **AI Style Themes**: Choose between English Cottage, Modern Xeriscape, Japanese Zen, and Wildflower Meadow.
5. **Photo Gallery**: Upload your own garden spaces via drag-and-drop or browsing files. Supports importing multiple files.
6. **Procedural Overlay Engine**:
   - Uses HTML5 Canvas to procedurally draw realistic, organic plant layers (tall background, midground clumps, low borders) tailored to the exact user inputs.
   - Adjusts color, shape, and height dynamically based on the Perennial vs. Annual ratio and moisture options.
7. **Multi-Format Exporter**:
   - Export individual concepts or all 3 concepts simultaneously.
   - Formats: **PNG, JPEG, and WEBP**.

---

## Local Development & Running

To run the application locally:
1. Double-click the `index.html` file to open it in your web browser.
2. Or use a local web server (like VS Code Live Server or python `python -m http.server`) to serve the project.

---

## Git Repository Structure

This project has been initialized with a local Git repository. The file history is split into logical commits:

- **Commit 1**: Project configuration and `.gitignore` setup.
- **Commit 2**: Generated base landscape templates in `assets/`.
- **Commit 3**: HTML shell and CSS styling.
- **Commit 4**: JavaScript app logic (canvas and zone mappings).

You can inspect the git commit tree by running:
```bash
git log --graph --oneline --all --decorate
```

---

## Pushing to GitHub

To publish this project to your GitHub account:

1. **Create a new, empty repository on GitHub**:
   - Go to [github.com/new](https://github.com/new).
   - Name it `LandscapePro` (or similar).
   - **Do NOT** check "Add a README file", "Add .gitignore", or "Choose a license" (since we have already initialized these files).
   - Click **Create repository**.

2. **Run the following commands in your terminal/powershell inside this project directory**:
   ```bash
   # Add your GitHub repository as the remote destination
   git remote add origin https://github.com/YOUR_GITHUB_USERNAME/LandscapePro.git
   
   # Rename the default branch to 'main'
   git branch -M main
   
   # Push the commits to your GitHub repository
   git push -u origin main
   ```
   *(Replace `YOUR_GITHUB_USERNAME` with your actual GitHub username).*
