/* -------------------------------------------------------------
 * LANDSCAPEPRO AI - JAVASCRIPT APPLICATION CORE
 * Includes: State Management, Climate Zone Lookup, File Uploader,
 * Interactive Before/After Swipe Slider, Procedural Plant Overlay
 * Engine, and Multi-Format Canvas Exporter.
 * ------------------------------------------------------------- */

// App State
const state = {
    images: [], // List of loaded/uploaded images
    activeImageId: 'demo', // 'demo' or file-based UUID
    activeConcept: 'concept-1',
    zipCodes: {
        '90210': { zone: '10a', climate: 'Warm Mediterranean', desc: 'Zone 10a: Min Temp 30°F to 35°F. Ideal for warm Mediterranean and subtropical species. Sandy loam soils are common here.' },
        '10001': { zone: '7b', climate: 'Humid Temperate', desc: 'Zone 7b: Min Temp 5°F to 10°F. Great for temperate shrubs, ornamental maples, hostas, and heirloom roses.' },
        '60601': { zone: '6a', climate: 'Cold Temperate / Continental', desc: 'Zone 6a: Min Temp -10°F to -5°F. Requires cold-hardy perennials, evergreens, and resilient summer annuals.' },
        '33101': { zone: '11a', climate: 'Subtropical / Tropical', desc: 'Zone 11a: Min Temp 40°F to 45°F. Prefers high humidity, organic peaty soils, palms, hibiscus, and heliconias.' },
        '98101': { zone: '8b', climate: 'Marine West Coast', desc: 'Zone 8b: Min Temp 15°F to 20°F. Cool wet winters, mild dry summers. Ideal for ferns, mosses, rhododendrons, and conifers.' },
        '80201': { zone: '5b', climate: 'Semi-arid / Cool Alpine', desc: 'Zone 5b: Min Temp -15°F to -10°F. High elevation, dry soils. Needs drought-resistant, cold-hardy plants like yuccas and conifers.' },
        '75201': { zone: '8a', climate: 'Hot Humid Subtropical', desc: 'Zone 8a: Min Temp 10°F to 15°F. Hot summers. Ideal for crape myrtles, salvias, and heat-tolerant perennials.' },
        '04401': { zone: '5a', climate: 'Cold Continental', desc: 'Zone 5a: Min Temp -20°F to -15°F. Long freezing winters. Requires northern forest plants like birches, spruces, and native wildflowers.' },
        '99501': { zone: '4b', climate: 'Subarctic', desc: 'Zone 4b: Min Temp -25°F to -20°F. Short growing season. Emphasize native subarctic shrubs, mosses, and high-altitude wild flora.' },
        '96801': { zone: '12b', climate: 'Tropical Wet', desc: 'Zone 12b: Min Temp 55°F to 60°F. Year-round tropical. Flourishes with plumerias, palms, gingers, and exotic groundcover.' },
        '79901': { zone: '8b', climate: 'Arid Desert', desc: 'Zone 8b (El Paso): Min Temp 15°F to 20°F. Dry and sunny. Native Chihuahuan desert plants, agaves, and drought-tolerant shrubs are ideal.' },
        '85001': { zone: '9b', climate: 'Hot Arid Desert', desc: 'Zone 9b (Phoenix): Min Temp 25°F to 30°F. Extremely hot and dry. Perfect for majestic saguaros, barrel cacti, and desert yuccas.' },
        '59001': { zone: '4b', climate: 'High Plains / Cold Semiarid', desc: 'Zone 4b (Billings): Min Temp -25°F to -20°F. Hardiest pines, junipers, and resilient native plains grasses are recommended.' },
        '32801': { zone: '9b', climate: 'Humid Subtropical', desc: 'Zone 9b (Orlando): Min Temp 25°F to 30°F. Wet and humid. Thrives with palmettos, hibiscus, broad ferns, and tropical groundcovers.' },
        '95814': { zone: '9b', climate: 'Interior Mediterranean', desc: 'Zone 9b (Sacramento): Min Temp 25°F to 30°F. Dry summers, wet winters. Excellent for olives, lavender, citrus, and salvias.' },
        '20001': { zone: '7b', climate: 'Mid-Atlantic Temperate', desc: 'Zone 7b (Washington DC): Min Temp 5°F to 10°F. Humid summers. Great for boxwood, dogwoods, azaleas, and hydrangeas.' },
        '02108': { zone: '6b', climate: 'Coastal New England', desc: 'Zone 6b (Boston): Min Temp -5°F to 0°F. Coastal winters. Suitable for hostas, rhododendrons, hydrangeas, and maples.' },
        '97201': { zone: '8b', climate: 'Pacific Northwest Wet', desc: 'Zone 8b (Portland): Min Temp 15°F to 20°F. Mild and very wet. Ideal for lush ferns, mosses, hostas, and maples.' },
        '55401': { zone: '4b', climate: 'Severe Continental', desc: 'Zone 4b (Minneapolis): Min Temp -25°F to -20°F. Extreme seasonal range. Hardiest local birches, pines, and native wildflowers.' },
        '84101': { zone: '7a', climate: 'Intermountain Cold Semiarid', desc: 'Zone 7a (Salt Lake City): Min Temp 0°F to 5°F. Alpine valleys. Prefers yuccas, sagebrush, dwarf conifers, and rock plants.' }
    },
    // Cache for generated concept images: { imageId: { concept1: dataUrl, concept2: dataUrl, concept3: dataUrl } }
    conceptCache: {},
    activeTheme: 'cottage'
};

// UI Elements
const zipSelect = document.getElementById('zip-code');
const climateCard = document.getElementById('climate-zone-card');
const climateBadge = document.getElementById('climate-zone-badge');
const climateDesc = document.getElementById('climate-desc');

const soilSelect = document.getElementById('soil-type');
const sunSelect = document.getElementById('sun-amount');
const waterSelect = document.getElementById('water-amount');
const perennialSlider = document.getElementById('perennial-ratio');
const perennialLabel = document.getElementById('plant-ratio-label');

const btnGenerate = document.getElementById('btn-generate');
const scannerBar = document.getElementById('scanner-bar');
const themeSelect = document.getElementById('garden-theme');

const dropzone = document.getElementById('dropzone');
const fileInput = document.getElementById('file-input');
const galleryGrid = document.getElementById('gallery-grid');
const photoCount = document.getElementById('photo-count');

const sliderContainer = document.getElementById('slider-container');
const afterLayer = document.getElementById('after-layer');
const sliderHandle = document.getElementById('slider-handle');
const imgBefore = document.getElementById('img-before');
const imgAfter = document.getElementById('img-after');

const conceptTabs = document.querySelectorAll('.concept-tab');
const conceptTitle = document.getElementById('concept-title');
const conceptDescription = document.getElementById('concept-description');

const btnExportSingle = document.getElementById('btn-export-single');
const btnExportAll = document.getElementById('btn-export-all');
const selectFormatSingle = document.getElementById('export-format-single');
const selectFormatAll = document.getElementById('export-format-all');

const exportCanvas = document.getElementById('export-canvas');

// Google Photos DOM References
const btnGooglePhotos = document.getElementById('btn-google-photos');
const modalGooglePhotos = document.getElementById('modal-google-photos');
const btnCloseModal = document.getElementById('btn-close-modal');
const btnGphotosLogin = document.getElementById('btn-gphotos-login');
const btnGphotosLogout = document.getElementById('btn-gphotos-logout');
const gphotosAuthView = document.getElementById('gphotos-auth-view');
const gphotosGridView = document.getElementById('gphotos-grid-view');
const gphotosItems = document.querySelectorAll('.gphotos-item');
const btnGphotosImport = document.getElementById('btn-gphotos-import');

// -------------------------------------------------------------
// INITIALIZATION
// -------------------------------------------------------------
window.addEventListener('DOMContentLoaded', () => {
    initCompareSlider();
    initZipCodeSelector();
    initThemeSelector();
    initFileUploader();
    initGenerateAction();
    initTabs();
    initExportActions();
    initGooglePhotos();
    
    // Load default demo space
    loadDemoSpace();
});

// Load Demo Space Concepts
function loadDemoSpace() {
    state.conceptCache['demo'] = {
        'concept-1': 'assets/template_cottage.png',
        'concept-2': 'assets/template_xeriscape.png',
        'concept-3': 'assets/template_zen.png'
    };
    updateActiveVisualization();
}

// -------------------------------------------------------------
// ZIP CODE & CLIMATE ZONE LOOKUP
// -------------------------------------------------------------
function initZipCodeSelector() {
    zipSelect.addEventListener('change', (e) => {
        const zip = e.target.value;
        const info = state.zipCodes[zip];
        
        if (info) {
            climateBadge.textContent = `Zone ${info.zone}`;
            climateDesc.innerHTML = `<strong>Climate:</strong> ${info.climate}<br>${info.desc}`;
            climateCard.classList.remove('hidden');
        } else {
            climateCard.classList.add('hidden');
        }
    });

    perennialSlider.addEventListener('input', (e) => {
        const val = e.target.value;
        perennialLabel.textContent = `${val}% Perennials / ${100 - val}% Annuals`;
    });
}

// -------------------------------------------------------------
// LANDSCAPE THEME SELECTOR
// -------------------------------------------------------------
function initThemeSelector() {
    themeSelect.addEventListener('change', (e) => {
        state.activeTheme = e.target.value;
    });
}

// -------------------------------------------------------------
// INTERACTIVE SWIPE SLIDER
// -------------------------------------------------------------
function initCompareSlider() {
    let isDragging = false;

    const setSliderPosition = (x) => {
        const rect = sliderContainer.getBoundingClientRect();
        let posX = x - rect.left;
        posX = Math.max(0, Math.min(posX, rect.width));
        const percentage = (posX / rect.width) * 100;

        // Position the handle
        sliderHandle.style.left = `${percentage}%`;
        // Clip the "After" image layer (which shows on the right side of the slider handle)
        afterLayer.style.clipPath = `polygon(${percentage}% 0, 100% 0, 100% 100%, ${percentage}% 100%)`;
    };

    const onMouseMove = (e) => {
        if (!isDragging) return;
        setSliderPosition(e.clientX);
    };

    const onTouchMove = (e) => {
        if (!isDragging) return;
        if (e.touches.length > 0) {
            setSliderPosition(e.touches[0].clientX);
        }
    };

    const stopDrag = () => {
        isDragging = false;
        document.removeEventListener('mousemove', onMouseMove);
        document.removeEventListener('mouseup', stopDrag);
        document.removeEventListener('touchmove', onTouchMove);
        document.removeEventListener('touchend', stopDrag);
    };

    const startDrag = (e) => {
        e.preventDefault();
        isDragging = true;
        document.addEventListener('mousemove', onMouseMove);
        document.addEventListener('mouseup', stopDrag);
        document.addEventListener('touchmove', onTouchMove);
        document.addEventListener('touchend', stopDrag);
    };

    sliderHandle.addEventListener('mousedown', startDrag);
    sliderHandle.addEventListener('touchstart', startDrag);

    // Also support clicking anywhere on the slider container to move it
    sliderContainer.addEventListener('click', (e) => {
        if (e.target === sliderHandle || sliderHandle.contains(e.target)) return;
        setSliderPosition(e.clientX);
    });
}

// -------------------------------------------------------------
// IMAGE FILE IMPORT HANDLER
// -------------------------------------------------------------
function initFileUploader() {
    // Dropzone events
    dropzone.addEventListener('dragover', (e) => {
        e.preventDefault();
        dropzone.classList.add('dragover');
    });

    dropzone.addEventListener('dragleave', () => {
        dropzone.classList.remove('dragover');
    });

    dropzone.addEventListener('drop', (e) => {
        e.preventDefault();
        dropzone.classList.remove('dragover');
        if (e.dataTransfer.files.length > 0) {
            handleUploadedFiles(e.dataTransfer.files);
        }
    });

    dropzone.addEventListener('click', () => {
        fileInput.click();
    });

    fileInput.addEventListener('change', (e) => {
        if (e.target.files.length > 0) {
            handleUploadedFiles(e.target.files);
        }
    });
}

function handleUploadedFiles(files) {
    let processedCount = 0;
    
    Array.from(files).forEach(file => {
        if (!file.type.startsWith('image/')) return;
        
        const reader = new FileReader();
        reader.onload = (event) => {
            const id = 'img_' + Date.now() + '_' + Math.random().toString(36).substr(2, 9);
            const src = event.target.result;
            
            const imageObject = {
                id: id,
                src: src,
                name: file.name,
                isDemo: false
            };
            
            state.images.push(imageObject);
            addGalleryItem(imageObject);
            
            processedCount++;
            if (processedCount === files.length) {
                // Set the last uploaded photo as the active workspace image
                selectWorkspaceImage(id);
                // Trigger AI generation on the new workspace image automatically!
                triggerAIGeneration();
            }
        };
        reader.readAsDataURL(file);
    });
}

function addGalleryItem(imgObj) {
    const item = document.createElement('div');
    item.className = 'gallery-item';
    item.id = `gallery-item-${imgObj.id}`;
    
    const img = document.createElement('img');
    img.src = imgObj.src;
    img.alt = imgObj.name;
    img.className = 'gallery-thumb';
    
    const badge = document.createElement('span');
    badge.className = 'gallery-badge';
    badge.textContent = imgObj.name.length > 15 ? imgObj.name.substring(0, 12) + '...' : imgObj.name;
    
    item.appendChild(img);
    item.appendChild(badge);
    
    item.addEventListener('click', () => {
        selectWorkspaceImage(imgObj.id);
    });
    
    galleryGrid.appendChild(item);
    
    // Update counts
    const nonDemoCount = state.images.filter(img => !img.isDemo).length;
    photoCount.textContent = `${nonDemoCount} Photos`;
}

function selectWorkspaceImage(id) {
    state.activeImageId = id;
    
    // Update active highlight in gallery
    document.querySelectorAll('.gallery-item').forEach(el => el.classList.remove('active'));
    if (id === 'demo') {
        document.getElementById('demo-gallery-item').classList.add('active');
        imgBefore.src = 'assets/template_bare.png';
    } else {
        const item = document.getElementById(`gallery-item-${id}`);
        if (item) item.classList.add('active');
        
        const activeImg = state.images.find(img => img.id === id);
        if (activeImg) {
            imgBefore.src = activeImg.src;
        }
    }
    
    // Check if we have cached concepts for this image. If not, generate them!
    if (!state.conceptCache[id]) {
        triggerAIGeneration();
    } else {
        updateActiveVisualization();
    }
}

// -------------------------------------------------------------
// TABS & CONCEPT SELECTION
// -------------------------------------------------------------
function initTabs() {
    conceptTabs.forEach(tab => {
        tab.addEventListener('click', () => {
            conceptTabs.forEach(t => t.classList.remove('active'));
            tab.classList.add('active');
            state.activeConcept = tab.dataset.concept;
            updateActiveVisualization();
        });
    });
}

function updateActiveVisualization() {
    const cache = state.conceptCache[state.activeImageId];
    if (cache && cache[state.activeConcept]) {
        imgAfter.src = cache[state.activeConcept];
    }
    
    // Update descriptions
    updateConceptLabels();
}

function updateConceptLabels() {
    const soil = soilSelect.options[soilSelect.selectedIndex].text.split(' ')[0];
    const sun = sunSelect.options[sunSelect.selectedIndex].text.split(' ')[0];
    const water = waterSelect.options[waterSelect.selectedIndex].text.split(' ')[0];
    const ratio = perennialSlider.value;
    const style = state.activeTheme.toUpperCase();
    
    const zip = zipSelect.value;
    const zoneText = zip ? `Zone ${state.zipCodes[zip].zone}` : 'Your climate';

    if (state.activeConcept === 'concept-1') {
        conceptTitle.textContent = `${style} Theme - Eco Balanced (Concept 1)`;
        conceptDescription.textContent = `A resilient native arrangement matched for ${zoneText} in ${soil} soil. Combines ${ratio}% perennials and ${100 - ratio}% annuals that thrive in ${sun} and ${water} moisture. Low maintenance garden structure.`;
    } else if (state.activeConcept === 'concept-2') {
        conceptTitle.textContent = `${style} Theme - Lush Layering (Concept 2)`;
        conceptDescription.textContent = `A dense, highly textured multi-tiered garden structure. Features tall focal shrubs, dense mid-border blooming perennials (${ratio}%), and premium ornamental accents requiring ${sun} and ${water} conditions.`;
    } else {
        conceptTitle.textContent = `${style} Theme - Minimalist Structural (Concept 3)`;
        conceptDescription.textContent = `Clean architectural design highlighting strong shapes and spaces. Clean gravel borders with accent boulders and select specimens tailored to ${soil} soil, optimized for ${sun} exposure.`;
    }
}

// -------------------------------------------------------------
// SIMULATED AI GENERATION & CANVAS RENDERING
// -------------------------------------------------------------
function initGenerateAction() {
    btnGenerate.addEventListener('click', () => {
        triggerAIGeneration();
    });
}

function triggerAIGeneration() {
    // Show spinner & disable button
    btnGenerate.disabled = true;
    btnGenerate.querySelector('.btn-text').textContent = 'AI Model Processing...';
    btnGenerate.querySelector('.loading-spinner').classList.remove('hidden');
    
    // Show scanner laser animation on the comparison container
    scannerBar.classList.remove('hidden');
    
    // Simulate generation delay
    setTimeout(async () => {
        const baseSrc = imgBefore.src;
        
        // Retrieve settings
        const soil = soilSelect.value;
        const sun = sunSelect.value;
        const water = waterSelect.value;
        const perennialRatio = parseInt(perennialSlider.value);
        const theme = state.activeTheme;
        
        // If it is the demo image, we load the premium static generated templates for extreme visual fidelity.
        // If it is a user uploaded image, we run our procedural overlay engine to render plants onto their photo.
        if (state.activeImageId === 'demo') {
            // Demo templates mappings
            if (theme === 'cottage') {
                state.conceptCache['demo'] = {
                    'concept-1': 'assets/template_cottage.png',
                    'concept-2': 'assets/template_cottage.png', // Add slight variance or use same high-fidelity image
                    'concept-3': 'assets/template_cottage.png'
                };
            } else if (theme === 'xeriscape') {
                state.conceptCache['demo'] = {
                    'concept-1': 'assets/template_xeriscape.png',
                    'concept-2': 'assets/template_xeriscape.png',
                    'concept-3': 'assets/template_xeriscape.png'
                };
            } else if (theme === 'zen') {
                state.conceptCache['demo'] = {
                    'concept-1': 'assets/template_zen.png',
                    'concept-2': 'assets/template_zen.png',
                    'concept-3': 'assets/template_zen.png'
                };
            } else {
                // Any other theme: draw procedurally on top of the bare yard template image
                const c1 = await generateProceduralConcepts(baseSrc, soil, sun, water, perennialRatio, theme, 1);
                const c2 = await generateProceduralConcepts(baseSrc, soil, sun, water, perennialRatio, theme, 2);
                const c3 = await generateProceduralConcepts(baseSrc, soil, sun, water, perennialRatio, theme, 3);
                
                state.conceptCache['demo'] = {
                    'concept-1': c1,
                    'concept-2': c2,
                    'concept-3': c3
                };
            }
        } else {
            // User uploaded image: procedurally draw overlays for 3 concepts
            const c1 = await generateProceduralConcepts(baseSrc, soil, sun, water, perennialRatio, theme, 1);
            const c2 = await generateProceduralConcepts(baseSrc, soil, sun, water, perennialRatio, theme, 2);
            const c3 = await generateProceduralConcepts(baseSrc, soil, sun, water, perennialRatio, theme, 3);
            
            state.conceptCache[state.activeImageId] = {
                'concept-1': c1,
                'concept-2': c2,
                'concept-3': c3
            };
        }
        
        // Hide scanner and spinner
        scannerBar.classList.add('hidden');
        btnGenerate.disabled = false;
        btnGenerate.querySelector('.btn-text').textContent = 'Generate AI Landscape';
        btnGenerate.querySelector('.loading-spinner').classList.add('hidden');
        
        // Display result
        updateActiveVisualization();
        
        // Trigger a cool swipe animation across the slider (0% to 100% to 50%) to showcase the transformation!
        animateSliderEntrance();
        
    }, 1800);
}

function animateSliderEntrance() {
    let progress = 100;
    afterLayer.style.clipPath = `polygon(100% 0, 100% 0, 100% 100%, 100% 100%)`;
    sliderHandle.style.left = `100%`;

    const interval = setInterval(() => {
        progress -= 2.5;
        if (progress <= 50) {
            progress = 50;
            clearInterval(interval);
        }
        sliderHandle.style.left = `${progress}%`;
        afterLayer.style.clipPath = `polygon(${progress}% 0, 100% 0, 100% 100%, ${progress}% 100%)`;
    }, 15);
}

// -------------------------------------------------------------
// CANVAS PROCEDURAL GENERATION ENGINE
// Draws beautiful, organic plant overlays based on environmental factors
// -------------------------------------------------------------
function generateProceduralConcepts(baseImageSrc, soil, sun, water, ratio, theme, conceptIndex) {
    return new Promise((resolve) => {
        const img = new Image();
        img.crossOrigin = "anonymous";
        img.onload = () => {
            const ctx = exportCanvas.getContext('2d');
            
            // Set canvas size matching the loaded image
            exportCanvas.width = img.naturalWidth;
            exportCanvas.height = img.naturalHeight;
            
            // Draw original base space
            ctx.drawImage(img, 0, 0);
            
            // Draw organic, customized plant overlays
            drawPlantOverlay(ctx, exportCanvas.width, exportCanvas.height, soil, sun, water, ratio, theme, conceptIndex);
            
            // Return generated concept data URI
            resolve(exportCanvas.toDataURL('image/png'));
        };
        img.src = baseImageSrc;
    });
}

function drawPlantOverlay(ctx, w, h, soil, sun, water, ratio, theme, conceptIndex) {
    // Generate organic elements based on coordinates in the bottom half of the image
    const groundLevel = h * 0.65;
    const canvasHeight = h;
    
    // Seeded random helper for consistency per concept
    let seed = conceptIndex * 15;
    function random() {
        let x = Math.sin(seed++) * 10000;
        return x - Math.floor(x);
    }

    // 1. Draw a beautiful curved soil bed or gravel edge
    ctx.beginPath();
    ctx.moveTo(-50, canvasHeight + 50);
    ctx.lineTo(-50, groundLevel + (random() * 40 - 20));
    
    // Control points for nice organic wave
    const cp1x = w * 0.3;
    const cp1y = groundLevel - 40 + (random() * 40);
    const cp2x = w * 0.7;
    const cp2y = groundLevel + 10 + (random() * 40);
    
    ctx.bezierCurveTo(cp1x, cp1y, cp2x, cp2y, w + 50, groundLevel - 20);
    ctx.lineTo(w + 50, canvasHeight + 50);
    ctx.closePath();
    
    // Set mulch or gravel color based on soil and theme
    let bedGrad = ctx.createLinearGradient(0, groundLevel, 0, canvasHeight);
    if (theme === 'xeriscape') {
        // Light sandy gravel
        bedGrad.addColorStop(0, '#e5e7eb');
        bedGrad.addColorStop(1, '#9ca3af');
    } else if (theme === 'zen') {
        // Mossy forest floor or dark slate
        bedGrad.addColorStop(0, '#365314');
        bedGrad.addColorStop(0.3, '#14532d');
        bedGrad.addColorStop(1, '#064e3b');
    } else {
        // Rich brown garden loam soil
        bedGrad.addColorStop(0, '#451a03');
        bedGrad.addColorStop(1, '#1c1917');
    }
    
    ctx.fillStyle = bedGrad;
    ctx.fill();

    // 2. Draw procedural plants
    // Decide density based on conceptIndex (Concept 2 is lush/dense, 3 is sparse/minimalist)
    let plantCount = 18;
    if (conceptIndex === 2) plantCount = 28; // Lush
    if (conceptIndex === 3) plantCount = 9;  // Minimalist

    // Build plant coordinate slots and sort them from back to front (y-axis coordinate)
    // this handles layering correctly so background plants are drawn first.
    let plants = [];
    for (let i = 0; i < plantCount; i++) {
        // Distribute coordinates
        const px = w * 0.05 + random() * (w * 0.9);
        // Place bases along the sloped ground bed
        const py = groundLevel + (canvasHeight - groundLevel) * (0.05 + 0.9 * random());
        
        // Determine type of plant based on theme, sun, water, and ratio
        let plantType = 'midground';
        if (py < groundLevel + (canvasHeight - groundLevel) * 0.3) {
            plantType = 'background'; // Back of bed = tall
        } else if (py > groundLevel + (canvasHeight - groundLevel) * 0.7) {
            plantType = 'foreground'; // Front of bed = short
        }
        
        plants.push({ x: px, y: py, type: plantType, rVal: random() });
    }
    
    // Sort plants by Y coordinate (painter's algorithm)
    plants.sort((a, b) => a.y - b.y);
    
    // Draw each plant
    plants.forEach(plant => {
        ctx.save();
        ctx.translate(plant.x, plant.y);
        
        // Add subtle variations based on random val
        const scale = 0.7 + plant.rVal * 0.6;
        ctx.scale(scale, scale);
        
        // Adjust style parameters
        if (theme === 'cottage') {
            drawCottagePlant(ctx, plant.type, plant.rVal, ratio);
        } else if (theme === 'xeriscape') {
            drawXeriscapePlant(ctx, plant.type, plant.rVal);
        } else if (theme === 'zen') {
            drawZenPlant(ctx, plant.type, plant.rVal);
        } else if (theme === 'meadow') {
            drawMeadowPlant(ctx, plant.type, plant.rVal, ratio);
        } else if (theme === 'mediterranean') {
            drawMediterraneanPlant(ctx, plant.type, plant.rVal);
        } else if (theme === 'rain-garden') {
            drawRainGardenPlant(ctx, plant.type, plant.rVal);
        } else if (theme === 'desert-oasis') {
            drawDesertOasisPlant(ctx, plant.type, plant.rVal);
        } else if (theme === 'woodland-shade') {
            drawWoodlandShadePlant(ctx, plant.type, plant.rVal);
        } else if (theme === 'formal-french') {
            drawFormalFrenchPlant(ctx, plant.type, plant.rVal);
        } else if (theme === 'tropical-jungle') {
            drawTropicalJunglePlant(ctx, plant.type, plant.rVal);
        } else if (theme === 'pollinator') {
            drawPollinatorPlant(ctx, plant.type, plant.rVal);
        } else if (theme === 'rock-alpine') {
            drawRockAlpinePlant(ctx, plant.type, plant.rVal);
        } else {
            drawCottagePlant(ctx, plant.type, plant.rVal, ratio);
        }
        
        ctx.restore();
    });
    
    // Draw some accent elements
    if (theme === 'zen') {
        // Draw some slate stepping stones in concept 3
        if (conceptIndex === 3) {
            drawZenStones(ctx, w, h);
        }
    }
}

// Plant drawers
function drawCottagePlant(ctx, type, rVal, perennialRatio) {
    ctx.shadowColor = 'rgba(0,0,0,0.15)';
    ctx.shadowBlur = 6;
    
    // Determine colors
    // High perennial ratio -> Lavender, salvias, delphiniums (purples/blues)
    // Low perennial ratio -> Petunias, marigolds (pinks, reds, yellows)
    const isPerennial = (rVal * 100) < perennialRatio;
    
    let bloomColor1 = '#ec4899'; // pink
    let bloomColor2 = '#f43f5e'; // red
    if (isPerennial) {
        bloomColor1 = '#8b5cf6'; // violet
        bloomColor2 = '#6366f1'; // indigo
    } else if (rVal > 0.6) {
        bloomColor1 = '#f59e0b'; // amber/orange
        bloomColor2 = '#ef4444'; // red
    }
    
    if (type === 'background') {
        // Delphiniums or tall flowering spires
        // Green stalks
        ctx.strokeStyle = '#065f46';
        ctx.lineWidth = 4;
        ctx.beginPath();
        ctx.moveTo(0, 0);
        ctx.lineTo(0, -90);
        ctx.stroke();
        
        // Flower spike circles
        ctx.fillStyle = bloomColor2;
        for (let i = 0; i < 15; i++) {
            const fy = -40 - (i * 3.5);
            const fx = Math.sin(i) * 5;
            ctx.beginPath();
            ctx.arc(fx, fy, 8 - (i * 0.3), 0, Math.PI * 2);
            ctx.fill();
        }
        // Inner blossoms
        ctx.fillStyle = bloomColor1;
        for (let i = 0; i < 15; i++) {
            const fy = -40 - (i * 3.5);
            const fx = Math.sin(i) * 5;
            ctx.beginPath();
            ctx.arc(fx + 2, fy, 4 - (i * 0.15), 0, Math.PI * 2);
            ctx.fill();
        }
    } else if (type === 'midground') {
        // Lush flowering bush
        // Green leaf mound
        ctx.fillStyle = '#0f766e';
        ctx.beginPath();
        ctx.arc(0, -10, 25, 0, Math.PI * 2);
        ctx.arc(-15, -20, 20, 0, Math.PI * 2);
        ctx.arc(15, -20, 20, 0, Math.PI * 2);
        ctx.fill();
        
        // Flowers
        ctx.fillStyle = bloomColor1;
        const flowerCount = 6 + Math.floor(rVal * 6);
        for (let i = 0; i < flowerCount; i++) {
            const fx = -20 + (i * 7) + (Math.sin(i * 10) * 4);
            const fy = -15 - (Math.abs(Math.cos(i * 5)) * 15);
            ctx.beginPath();
            ctx.arc(fx, fy, 6, 0, Math.PI * 2);
            ctx.fill();
            
            // Flower center
            ctx.fillStyle = '#fef08a'; // yellow center
            ctx.beginPath();
            ctx.arc(fx, fy, 2, 0, Math.PI * 2);
            ctx.fill();
            ctx.fillStyle = bloomColor1;
        }
    } else {
        // Low border flowers (e.g. Lavender clumps or sweet alyssum)
        ctx.fillStyle = '#10b981';
        ctx.beginPath();
        ctx.arc(0, -5, 12, 0, Math.PI * 2);
        ctx.fill();
        
        ctx.fillStyle = bloomColor2;
        for (let i = 0; i < 8; i++) {
            const fx = Math.sin(i) * 12;
            const fy = -5 - (rVal * 15) * Math.cos(i);
            ctx.beginPath();
            ctx.arc(fx, fy, 3.5, 0, Math.PI * 2);
            ctx.fill();
        }
    }
}

function drawXeriscapePlant(ctx, type, rVal) {
    ctx.shadowColor = 'rgba(0,0,0,0.2)';
    ctx.shadowBlur = 5;

    if (type === 'background') {
        // Tall sculptural Yucca or Joshua tree structure
        ctx.strokeStyle = '#5f370e'; // brown trunk
        ctx.lineWidth = 6;
        ctx.beginPath();
        ctx.moveTo(0, 0);
        ctx.lineTo(0, -50);
        ctx.lineTo(-15, -75);
        ctx.moveTo(0, -50);
        ctx.lineTo(15, -70);
        ctx.stroke();

        // Spiky heads
        const drawYuccaHead = (hx, hy) => {
            ctx.save();
            ctx.translate(hx, hy);
            ctx.strokeStyle = '#166534';
            ctx.lineWidth = 1.5;
            for (let angle = 0; angle < Math.PI * 2; angle += 0.2) {
                const len = 15 + rVal * 8;
                ctx.beginPath();
                ctx.moveTo(0, 0);
                ctx.lineTo(Math.cos(angle) * len, Math.sin(angle) * len);
                ctx.stroke();
            }
            ctx.restore();
        };

        drawYuccaHead(-15, -75);
        drawYuccaHead(15, -70);
        drawYuccaHead(0, -50);
        
    } else if (type === 'midground') {
        // Agave or Aloe structure
        ctx.fillStyle = '#065f46';
        ctx.strokeStyle = '#047857';
        ctx.lineWidth = 2;
        
        // Draw leaves radiating outward
        for (let i = 0; i < 12; i++) {
            ctx.save();
            const angle = -Math.PI + (i * Math.PI) / 11;
            ctx.rotate(angle);
            
            ctx.beginPath();
            ctx.moveTo(0, 0);
            ctx.quadraticCurveTo(8, -12, 0, -35 - (rVal * 15));
            ctx.quadraticCurveTo(-8, -12, 0, 0);
            ctx.fill();
            ctx.stroke();
            ctx.restore();
        }
    } else {
        // Small succulents or round desert cacti (e.g. Golden Barrel Cactus)
        const rad = 14 + rVal * 8;
        ctx.fillStyle = '#15803d';
        ctx.strokeStyle = '#f59e0b'; // golden spikes
        ctx.lineWidth = 1.5;
        
        ctx.beginPath();
        ctx.arc(0, -rad, rad, 0, Math.PI * 2);
        ctx.fill();
        
        // draw yellow spines around the cactus border
        for (let angle = 0; angle < Math.PI * 2; angle += 0.3) {
            ctx.beginPath();
            ctx.moveTo(Math.cos(angle) * rad, -rad + Math.sin(angle) * rad);
            ctx.lineTo(Math.cos(angle) * (rad + 4), -rad + Math.sin(angle) * (rad + 4));
            ctx.stroke();
        }
    }
}

function drawZenPlant(ctx, type, rVal) {
    if (type === 'background') {
        // Small weeping Japanese Maple tree
        ctx.strokeStyle = '#292524'; // grey-brown trunk
        ctx.lineWidth = 4;
        ctx.beginPath();
        ctx.moveTo(0, 0);
        // Curved trunk
        ctx.quadraticCurveTo(-15, -40, 5, -80);
        ctx.stroke();

        // Branches and leaves
        ctx.lineWidth = 2;
        ctx.beginPath();
        ctx.moveTo(5, -80);
        ctx.quadraticCurveTo(-20, -90, -35, -70); // branch 1
        ctx.moveTo(5, -80);
        ctx.quadraticCurveTo(30, -90, 40, -75); // branch 2
        ctx.stroke();

        // Crimson foliage (red maple)
        const drawMapleFoliage = (fx, fy, scale) => {
            ctx.fillStyle = 'rgba(153, 27, 27, 0.9)'; // crimson red
            for (let i = 0; i < 8; i++) {
                const ox = Math.sin(i) * 18 * scale;
                const oy = Math.cos(i) * 12 * scale;
                ctx.beginPath();
                ctx.arc(fx + ox, fy + oy, 12 * scale, 0, Math.PI * 2);
                ctx.fill();
            }
        };

        drawMapleFoliage(5, -80, 1.0);
        drawMapleFoliage(-35, -70, 0.8);
        drawMapleFoliage(40, -75, 0.8);
        
    } else if (type === 'midground') {
        // Bamboo stalks or tall Zen reeds
        ctx.strokeStyle = '#14532d';
        ctx.lineWidth = 3;
        
        const count = 3 + Math.floor(rVal * 3);
        for (let b = 0; b < count; b++) {
            const bx = -15 + b * 12;
            const length = 70 + rVal * 30;
            
            ctx.beginPath();
            ctx.moveTo(bx, 0);
            ctx.lineTo(bx - (b * 2), -length);
            ctx.stroke();
            
            // Draw small side leaves
            ctx.fillStyle = '#166534';
            for (let l = 0; l < 5; l++) {
                const ly = -10 - (l * 12);
                ctx.save();
                ctx.translate(bx - (b * 2 * (ly/-length)), ly);
                ctx.rotate(0.5 - (l % 2));
                ctx.beginPath();
                ctx.ellipse(0, 0, 8, 2, 0, 0, Math.PI * 2);
                ctx.fill();
                ctx.restore();
            }
        }
    } else {
        // Ferns or Shade Hostas
        ctx.fillStyle = '#065f46';
        
        // Broad leafy heart-shaped leaves for Hosta
        for (let i = 0; i < 10; i++) {
            ctx.save();
            const angle = -Math.PI * 0.9 + (i * Math.PI * 0.8) / 9;
            ctx.rotate(angle);
            
            ctx.beginPath();
            ctx.moveTo(0, 0);
            ctx.quadraticCurveTo(12, -8, 0, -22);
            ctx.quadraticCurveTo(-12, -8, 0, 0);
            ctx.fill();
            ctx.restore();
        }
        
        // Hostas have yellow outline edges in partial shade
        ctx.strokeStyle = '#84cc16';
        ctx.lineWidth = 0.8;
        for (let i = 0; i < 10; i++) {
            ctx.save();
            const angle = -Math.PI * 0.9 + (i * Math.PI * 0.8) / 9;
            ctx.rotate(angle);
            ctx.beginPath();
            ctx.moveTo(0, 0);
            ctx.quadraticCurveTo(12, -8, 0, -22);
            ctx.quadraticCurveTo(-12, -8, 0, 0);
            ctx.stroke();
            ctx.restore();
        }
    }
}

function drawMeadowPlant(ctx, type, rVal, perennialRatio) {
    // Soft wildflower meadow look: wispy grasses and scattered dots of bright red, yellow, and blue blooms
    
    // 1. Draw thin wispy grasses
    ctx.strokeStyle = '#065f46';
    ctx.lineWidth = 1.2;
    const grassCount = 12 + Math.floor(rVal * 10);
    for (let g = 0; g < grassCount; g++) {
        const angle = -0.3 + (g * 0.6) / grassCount;
        const length = 40 + rVal * 30;
        
        ctx.beginPath();
        ctx.moveTo(-10 + g * 2, 0);
        ctx.quadraticCurveTo(-10 + g * 2 + Math.sin(g) * 5, -length * 0.5, -10 + g * 2 + angle * 25, -length);
        ctx.stroke();
    }

    // 2. Draw colorful wildflower heads
    // Ratio controls color balance: High perennials -> blue cornflowers; low -> red poppies
    const isPerennial = (rVal * 100) < perennialRatio;
    let bloomColor = '#ef4444'; // Red poppy
    if (isPerennial) {
        bloomColor = '#3b82f6'; // Blue cornflower
    } else if (rVal > 0.7) {
        bloomColor = '#eab308'; // Yellow daisy
    }

    ctx.fillStyle = bloomColor;
    const flowerCount = 3 + Math.floor(rVal * 5);
    for (let f = 0; f < flowerCount; f++) {
        const fx = -12 + (f * 6) + (Math.sin(f) * 4);
        const fy = -25 - (f * 8);
        
        // Flower petals
        ctx.beginPath();
        ctx.arc(fx, fy, 4, 0, Math.PI * 2);
        ctx.fill();
        
        // Flower center
        ctx.fillStyle = '#111827'; // black eye
        ctx.beginPath();
        ctx.arc(fx, fy, 1.2, 0, Math.PI * 2);
        ctx.fill();
        ctx.fillStyle = bloomColor; // restore
    }
}

function drawZenStones(ctx, w, h) {
    // Draw some flat grey stepping stones in the foreground
    const ctxC = exportCanvas.getContext('2d');
    ctxC.fillStyle = '#4b5563';
    ctxC.strokeStyle = '#374151';
    ctxC.lineWidth = 2;

    const stones = [
        { x: w * 0.25, y: h * 0.85, rx: 35, ry: 12 },
        { x: w * 0.45, y: h * 0.78, rx: 28, ry: 9 },
        { x: w * 0.65, y: h * 0.88, rx: 42, ry: 15 }
    ];

    stones.forEach(stone => {
        ctxC.save();
        ctxC.translate(stone.x, stone.y);
        ctxC.beginPath();
        ctxC.ellipse(0, 0, stone.rx, stone.ry, 0.1, 0, Math.PI * 2);
        ctxC.fill();
        ctxC.stroke();
        
        // Stone texture highlights
        ctxC.strokeStyle = 'rgba(255,255,255,0.15)';
        ctxC.lineWidth = 1;
        ctxC.beginPath();
        ctxC.ellipse(-3, -2, stone.rx - 5, stone.ry - 3, 0.1, 0, Math.PI);
        ctxC.stroke();
        
        ctxC.restore();
    });
}

// -------------------------------------------------------------
// EXPORT PACKAGER ENGINE
// -------------------------------------------------------------
function initExportActions() {
    // Individual active concept export
    btnExportSingle.addEventListener('click', () => {
        const format = selectFormatSingle.value;
        const activeSrc = imgAfter.src;
        
        if (!activeSrc || activeSrc.includes('template_bare')) {
            alert('Please generate AI designs first before exporting!');
            return;
        }
        
        downloadImage(activeSrc, `LandscapePro_Design_${state.activeTheme}_${state.activeConcept}.${format}`, format);
    });

    // Mass export of all three concepts
    btnExportAll.addEventListener('click', () => {
        const format = selectFormatAll.value;
        const cache = state.conceptCache[state.activeImageId];
        
        if (!cache || !cache['concept-1']) {
            alert('Please generate AI designs first before exporting!');
            return;
        }
        
        // Download all three concepts sequentially with a slight staggered delay
        const concepts = ['concept-1', 'concept-2', 'concept-3'];
        concepts.forEach((concept, index) => {
            setTimeout(() => {
                const src = cache[concept];
                downloadImage(src, `LandscapePro_${state.activeTheme}_Concept_${index + 1}.${format}`, format);
            }, index * 400); // 400ms interval so the browser registers separate file downloads smoothly
        });
    });
}

function downloadImage(dataUrl, filename, format) {
    // If the dataUrl is already a data URI matching our needs, we download directly.
    // If we need to change format (e.g. source is PNG but user wants JPEG), we re-render on canvas first.
    let finalUrl = dataUrl;
    
    // Check if the format matches the source data URL. PNG is default generated.
    // If not matching, we pipe through canvas to convert.
    const isJpgRequested = format === 'jpeg';
    const isWebpRequested = format === 'webp';
    const isSourceDataUri = dataUrl.startsWith('data:');
    
    if (isSourceDataUri && (isJpgRequested || isWebpRequested)) {
        const tempImg = new Image();
        tempImg.onload = () => {
            const tempCanvas = document.createElement('canvas');
            tempCanvas.width = tempImg.naturalWidth;
            tempCanvas.height = tempImg.naturalHeight;
            const tempCtx = tempCanvas.getContext('2d');
            
            // Draw opaque background for JPEG (transparency turns black otherwise)
            if (isJpgRequested) {
                tempCtx.fillStyle = '#ffffff';
                tempCtx.fillRect(0, 0, tempCanvas.width, tempCanvas.height);
            }
            
            tempCtx.drawImage(tempImg, 0, 0);
            const mimeType = isJpgRequested ? 'image/jpeg' : 'image/webp';
            const convertedUrl = tempCanvas.toDataURL(mimeType, 0.92); // 92% quality
            triggerDownload(convertedUrl, filename);
        };
        tempImg.src = dataUrl;
    } else {
        // Direct download (already correct format or static asset)
        triggerDownload(finalUrl, filename);
    }
}

function triggerDownload(url, filename) {
    const link = document.createElement('a');
    link.href = url;
    link.download = filename;
    document.body.appendChild(link);
    link.click();
    document.body.removeChild(link);
}

// -------------------------------------------------------------
// NEW EXPANDED GARDEN THEMES DRAWING METHODS
// -------------------------------------------------------------

function drawMediterraneanPlant(ctx, type, rVal) {
    ctx.shadowColor = 'rgba(0,0,0,0.1)';
    ctx.shadowBlur = 4;
    
    if (type === 'background') {
        // Olive tree (dusty green leaves, slender branches)
        ctx.strokeStyle = '#3e2723';
        ctx.lineWidth = 3.5;
        ctx.beginPath();
        ctx.moveTo(0, 0);
        ctx.quadraticCurveTo(-10, -35, 0, -65);
        ctx.stroke();
        
        ctx.strokeStyle = '#4e342e';
        ctx.lineWidth = 1.8;
        ctx.beginPath();
        ctx.moveTo(0, -65);
        ctx.quadraticCurveTo(-20, -75, -25, -60);
        ctx.moveTo(0, -65);
        ctx.quadraticCurveTo(20, -75, 25, -55);
        ctx.stroke();
        
        // Dusty grey-green foliage
        const drawOliveLeaves = (lx, ly) => {
            ctx.fillStyle = 'rgba(107, 114, 92, 0.85)';
            for (let i = 0; i < 6; i++) {
                ctx.beginPath();
                ctx.arc(lx + Math.sin(i)*10, ly + Math.cos(i)*6, 8, 0, Math.PI*2);
                ctx.fill();
            }
        };
        drawOliveLeaves(-25, -60);
        drawOliveLeaves(25, -55);
        drawOliveLeaves(0, -65);
    } else if (type === 'midground') {
        // Lavender / Salvia clumps (grey-green base, purple flowering spikes)
        ctx.fillStyle = '#4b5320'; // olive green foliage
        ctx.beginPath();
        ctx.arc(0, -8, 16, 0, Math.PI*2);
        ctx.fill();
        
        // Purple spikes
        ctx.fillStyle = '#8b5cf6';
        for (let i = 0; i < 8; i++) {
            const h = 25 + rVal * 15;
            const angle = -0.5 + (i * 1.0) / 7;
            ctx.save();
            ctx.rotate(angle);
            ctx.fillRect(-2, -h, 4, h - 8);
            // Flower dots
            ctx.fillStyle = '#a78bfa';
            ctx.beginPath();
            ctx.arc(0, -h, 3.5, 0, Math.PI*2);
            ctx.arc(0, -h + 5, 3, 0, Math.PI*2);
            ctx.fill();
            ctx.restore();
            ctx.fillStyle = '#8b5cf6'; // restore
        }
    } else {
        // Terracotta pot with red geraniums
        ctx.fillStyle = '#c2410c'; // Terracotta orange
        ctx.beginPath();
        ctx.moveTo(-10, 0);
        ctx.lineTo(10, 0);
        ctx.lineTo(7, 12);
        ctx.lineTo(-7, 12);
        ctx.closePath();
        ctx.fill();
        
        // Green leaves inside pot
        ctx.fillStyle = '#15803d';
        ctx.beginPath();
        ctx.arc(0, -3, 9, 0, Math.PI*2);
        ctx.fill();
        
        // Red geranium dots
        ctx.fillStyle = '#ef4444';
        for (let i = 0; i < 5; i++) {
            ctx.beginPath();
            ctx.arc(-6 + i*3, -4 - (Math.sin(i)*2), 3, 0, Math.PI*2);
            ctx.fill();
        }
    }
}

function drawRainGardenPlant(ctx, type, rVal) {
    if (type === 'background') {
        // Tall river rushes / reeds
        ctx.strokeStyle = '#0f5132';
        ctx.lineWidth = 2.5;
        const rushCount = 4 + Math.floor(rVal * 3);
        for (let i = 0; i < rushCount; i++) {
            const rx = -12 + i * 8;
            const len = 65 + rVal * 25;
            ctx.beginPath();
            ctx.moveTo(rx, 0);
            ctx.quadraticCurveTo(rx + Math.sin(i)*6, -len*0.5, rx - 5, -len);
            ctx.stroke();
            
            // Brown seed head
            ctx.fillStyle = '#422006';
            ctx.beginPath();
            ctx.ellipse(rx - 5, -len, 3, 8, 0.1, 0, Math.PI * 2);
            ctx.fill();
        }
    } else if (type === 'midground') {
        // Wet Ferns (lush green pinnate leaves)
        ctx.fillStyle = '#198754';
        for (let f = 0; f < 8; f++) {
            ctx.save();
            const angle = -0.9 + (f * 1.8) / 7;
            ctx.rotate(angle);
            
            ctx.beginPath();
            ctx.moveTo(0, 0);
            ctx.quadraticCurveTo(8, -12, 0, -28 - rVal * 12);
            ctx.quadraticCurveTo(-8, -12, 0, 0);
            ctx.fill();
            
            // Side leaflets
            ctx.fillStyle = '#146c43';
            for (let i = 0; i < 4; i++) {
                ctx.beginPath();
                ctx.arc(4, -8 - (i*4), 3, 0, Math.PI*2);
                ctx.arc(-4, -8 - (i*4), 3, 0, Math.PI*2);
                ctx.fill();
            }
            ctx.fillStyle = '#198754';
            ctx.restore();
        }
    } else {
        // Bog flowers (Blue Flag Iris)
        ctx.strokeStyle = '#146c43';
        ctx.lineWidth = 2;
        ctx.beginPath();
        ctx.moveTo(0, 0);
        ctx.lineTo(-5, -28);
        ctx.stroke();
        
        // Iris blooms
        ctx.fillStyle = '#3b82f6';
        ctx.beginPath();
        ctx.arc(-5, -28, 5, 0, Math.PI*2);
        ctx.fill();
        
        ctx.fillStyle = '#60a5fa';
        ctx.beginPath();
        ctx.arc(-8, -25, 4, 0, Math.PI*2);
        ctx.arc(-2, -25, 4, 0, Math.PI*2);
        ctx.fill();
    }
}

function drawDesertOasisPlant(ctx, type, rVal) {
    ctx.shadowColor = 'rgba(0,0,0,0.15)';
    ctx.shadowBlur = 5;

    if (type === 'background') {
        // Majestic Saguaro Cactus
        ctx.fillStyle = '#2d6a4f';
        ctx.strokeStyle = '#1b4332';
        ctx.lineWidth = 2;
        
        // Main stem
        const trunkW = 12;
        const trunkH = 80 + rVal * 30;
        ctx.beginPath();
        ctx.roundRect(-trunkW/2, -trunkH, trunkW, trunkH, [6, 6, 0, 0]);
        ctx.fill();
        ctx.stroke();
        
        // Left arm
        ctx.beginPath();
        ctx.moveTo(-trunkW/2, -trunkH * 0.4);
        ctx.lineTo(-trunkW/2 - 14, -trunkH * 0.4);
        ctx.lineTo(-trunkW/2 - 14, -trunkH * 0.7);
        ctx.lineTo(-trunkW/2 - 4, -trunkH * 0.7);
        ctx.lineTo(-trunkW/2 - 4, -trunkH * 0.48);
        ctx.lineTo(-trunkW/2, -trunkH * 0.48);
        ctx.closePath();
        ctx.fill();
        ctx.stroke();
        
        // Right arm
        ctx.beginPath();
        ctx.moveTo(trunkW/2, -trunkH * 0.5);
        ctx.lineTo(trunkW/2 + 12, -trunkH * 0.5);
        ctx.lineTo(trunkW/2 + 12, -trunkH * 0.78);
        ctx.lineTo(trunkW/2 + 4, -trunkH * 0.78);
        ctx.lineTo(trunkW/2 + 4, -trunkH * 0.58);
        ctx.lineTo(trunkW/2, -trunkH * 0.58);
        ctx.closePath();
        ctx.fill();
        ctx.stroke();
    } else if (type === 'midground') {
        // Prickly Pear Cactus
        ctx.fillStyle = '#40916c';
        ctx.strokeStyle = '#f59e0b'; // tiny golden needles outline
        ctx.lineWidth = 0.8;
        
        // Base lobe
        ctx.beginPath();
        ctx.ellipse(0, -12, 12, 16, 0, 0, Math.PI*2);
        ctx.fill();
        ctx.stroke();
        
        // Upper lobes
        ctx.save();
        ctx.translate(-8, -24);
        ctx.rotate(-0.4);
        ctx.beginPath();
        ctx.ellipse(0, 0, 9, 12, 0, 0, Math.PI*2);
        ctx.fill();
        ctx.stroke();
        ctx.restore();
        
        ctx.save();
        ctx.translate(8, -22);
        ctx.rotate(0.3);
        ctx.beginPath();
        ctx.ellipse(0, 0, 8, 11, 0, 0, Math.PI*2);
        ctx.fill();
        ctx.stroke();
        ctx.restore();
        
        // Pink fruits on tops of pads
        ctx.fillStyle = '#ec4899';
        ctx.beginPath();
        ctx.arc(-14, -34, 3, 0, Math.PI*2);
        ctx.arc(14, -30, 3, 0, Math.PI*2);
        ctx.fill();
    } else {
        // Agave pup (small, spiky rosette)
        ctx.fillStyle = '#52b788';
        ctx.strokeStyle = '#2d6a4f';
        ctx.lineWidth = 1.2;
        
        for (let i = 0; i < 8; i++) {
            ctx.save();
            const angle = -Math.PI * 0.8 + (i * Math.PI * 0.6) / 7;
            ctx.rotate(angle);
            ctx.beginPath();
            ctx.moveTo(0, 0);
            ctx.quadraticCurveTo(6, -6, 0, -18);
            ctx.quadraticCurveTo(-6, -6, 0, 0);
            ctx.fill();
            ctx.stroke();
            ctx.restore();
        }
    }
}

function drawWoodlandShadePlant(ctx, type, rVal) {
    if (type === 'background') {
        // Red Maple sapling (airy silhouette)
        ctx.strokeStyle = '#3e2723';
        ctx.lineWidth = 3;
        ctx.beginPath();
        ctx.moveTo(0, 0);
        ctx.quadraticCurveTo(-8, -35, -4, -70);
        ctx.stroke();
        
        // Branches
        ctx.strokeStyle = '#4e342e';
        ctx.lineWidth = 1.5;
        ctx.beginPath();
        ctx.moveTo(-6, -45);
        ctx.lineTo(-20, -58);
        ctx.moveTo(-5, -55);
        ctx.lineTo(15, -68);
        ctx.stroke();
        
        // Starry red leaves
        ctx.fillStyle = '#b91c1c';
        const leafClusters = [
            { x: -4, y: -70 },
            { x: -20, y: -58 },
            { x: 15, y: -68 }
        ];
        leafClusters.forEach(c => {
            for (let i = 0; i < 5; i++) {
                ctx.beginPath();
                ctx.arc(c.x + Math.sin(i)*8, c.y + Math.cos(i)*5, 5, 0, Math.PI*2);
                ctx.fill();
            }
        });
    } else if (type === 'midground') {
        // Shade Woodland Hostas (broad variegated foliage)
        ctx.fillStyle = '#0f5132'; // dark green
        ctx.strokeStyle = '#e0e7ff'; // white margins
        ctx.lineWidth = 1.2;
        
        for (let i = 0; i < 9; i++) {
            ctx.save();
            const angle = -Math.PI * 0.8 + (i * Math.PI * 0.6) / 8;
            ctx.rotate(angle);
            ctx.beginPath();
            ctx.moveTo(0, 0);
            ctx.quadraticCurveTo(12, -8, 0, -22 - rVal*8);
            ctx.quadraticCurveTo(-12, -8, 0, 0);
            ctx.fill();
            ctx.stroke();
            ctx.restore();
        }
    } else {
        // Creeping forest moss mounds
        ctx.fillStyle = '#3f6212';
        ctx.beginPath();
        ctx.ellipse(0, 0, 20 + rVal * 15, 6, 0, 0, Math.PI * 2);
        ctx.fill();
        
        // Light moss accents
        ctx.fillStyle = '#65a30d';
        ctx.beginPath();
        ctx.ellipse(-4, -1, 10 + rVal * 8, 3, -0.1, 0, Math.PI * 2);
        ctx.fill();
    }
}

function drawFormalFrenchPlant(ctx, type, rVal) {
    ctx.shadowColor = 'rgba(0,0,0,0.1)';
    ctx.shadowBlur = 4;

    if (type === 'background') {
        // Neat Conical Boxwood Topiary
        ctx.fillStyle = '#14532d';
        ctx.beginPath();
        ctx.moveTo(0, -75);
        ctx.lineTo(-20, 0);
        ctx.lineTo(20, 0);
        ctx.closePath();
        ctx.fill();
        
        // Symmetrical trunk
        ctx.strokeStyle = '#292524';
        ctx.lineWidth = 3;
        ctx.beginPath();
        ctx.moveTo(0, 0);
        ctx.lineTo(0, 8);
        ctx.stroke();
    } else if (type === 'midground') {
        // Boxwood Globe
        ctx.fillStyle = '#165b33';
        ctx.beginPath();
        ctx.arc(0, -18, 18, 0, Math.PI*2);
        ctx.fill();
        
        // Small stem
        ctx.strokeStyle = '#292524';
        ctx.lineWidth = 2.5;
        ctx.beginPath();
        ctx.moveTo(0, 0);
        ctx.lineTo(0, -4);
        ctx.stroke();
    } else {
        // Neat rectangular border hedge
        ctx.fillStyle = '#1b4332';
        ctx.fillRect(-22, -10, 44, 10);
        
        ctx.strokeStyle = '#40916c';
        ctx.lineWidth = 1;
        ctx.strokeRect(-22, -10, 44, 10);
    }
}

function drawTropicalJunglePlant(ctx, type, rVal) {
    if (type === 'background') {
        // Broad fan palm leaf branch
        ctx.strokeStyle = '#1b4332';
        ctx.lineWidth = 3.5;
        ctx.beginPath();
        ctx.moveTo(0, 0);
        ctx.quadraticCurveTo(-15, -30, 20, -70);
        ctx.stroke();
        
        // Palm leaflets radiating outwards
        ctx.strokeStyle = '#2d6a4f';
        ctx.lineWidth = 2;
        for (let i = 0; i < 15; i++) {
            const px = 10 + i * 1.5;
            const py = -20 - i * 3.5;
            ctx.beginPath();
            ctx.moveTo(px, py);
            ctx.lineTo(px - 16 - (rVal*10), py + 12 + (i*0.5));
            ctx.stroke();
        }
    } else if (type === 'midground') {
        // Monstera leaves / Elephant ears
        ctx.fillStyle = '#0f5132';
        for (let i = 0; i < 5; i++) {
            ctx.save();
            const angle = -0.6 + i * 0.3;
            ctx.rotate(angle);
            
            // Large heart leaf
            ctx.beginPath();
            ctx.ellipse(0, -22 - rVal*8, 16, 22, 0.1, 0, Math.PI*2);
            ctx.fill();
            
            // Leaf slits (drawn as background colored lines)
            ctx.strokeStyle = 'rgba(20,26,38,0.7)'; // blend with mud/mulch background
            ctx.lineWidth = 2;
            ctx.beginPath();
            ctx.moveTo(-10, -26);
            ctx.lineTo(-4, -20);
            ctx.moveTo(10, -26);
            ctx.lineTo(4, -20);
            ctx.stroke();
            ctx.restore();
        }
    } else {
        // Bird of paradise flower (exotic shape, orange and neon blue)
        ctx.strokeStyle = '#1b4332';
        ctx.lineWidth = 2.5;
        ctx.beginPath();
        ctx.moveTo(0, 0);
        ctx.lineTo(-5, -30);
        ctx.stroke();
        
        // Orange flower beak
        ctx.fillStyle = '#f97316';
        ctx.beginPath();
        ctx.moveTo(-5, -30);
        ctx.lineTo(-20, -35);
        ctx.lineTo(-5, -42);
        ctx.closePath();
        ctx.fill();
        
        // Neon blue spikes
        ctx.fillStyle = '#06b6d4';
        ctx.beginPath();
        ctx.moveTo(-5, -30);
        ctx.lineTo(-2, -48);
        ctx.lineTo(-8, -44);
        ctx.closePath();
        ctx.fill();
    }
}

function drawPollinatorPlant(ctx, type, rVal) {
    // Dense flower fields for bees
    if (type === 'background') {
        // Tall Coneflowers (Purple coneflowers)
        ctx.strokeStyle = '#0f766e';
        ctx.lineWidth = 2;
        ctx.beginPath();
        ctx.moveTo(0, 0);
        ctx.lineTo(-4, -55 - rVal*15);
        ctx.stroke();
        
        // Flower petals
        ctx.fillStyle = '#df73ff'; // magenta
        ctx.beginPath();
        ctx.ellipse(-4, -55 - rVal*15, 12, 4, 0.2, 0, Math.PI*2);
        ctx.fill();
        
        // Prominent cone
        ctx.fillStyle = '#78350f'; // copper brown cone
        ctx.beginPath();
        ctx.arc(-4, -58 - rVal*15, 4.5, 0, Math.PI*2);
        ctx.fill();
    } else if (type === 'midground') {
        // Butterfly bush (dense clustering of lilac florets)
        ctx.fillStyle = '#065f46';
        ctx.beginPath();
        ctx.arc(0, -10, 20, 0, Math.PI*2);
        ctx.fill();
        
        // Lilac flower spires
        ctx.fillStyle = '#a78bfa';
        for (let i = 0; i < 6; i++) {
            const h = 18 + rVal * 10;
            const fx = -15 + i*6;
            ctx.fillRect(fx - 2, -h - 8, 4, h);
            ctx.beginPath();
            ctx.arc(fx, -h - 8, 4, 0, Math.PI*2);
            ctx.fill();
        }
        
        // Draw tiny honeybee dots buzzing around!
        ctx.fillStyle = '#fbbf24'; // yellow body
        ctx.beginPath();
        ctx.ellipse(18, -32, 2.5, 1.8, 0.2, 0, Math.PI*2);
        ctx.fill();
        ctx.fillStyle = '#111827'; // black stripes
        ctx.fillRect(17, -33, 1, 3.5);
    } else {
        // Creeping phlox (dense carpet of tiny pink flowers)
        ctx.fillStyle = '#ec4899';
        ctx.beginPath();
        ctx.ellipse(0, 0, 16 + rVal * 10, 5, 0, 0, Math.PI*2);
        ctx.fill();
        // Individual blossoms
        ctx.fillStyle = '#f472b6';
        for (let i = 0; i < 6; i++) {
            ctx.beginPath();
            ctx.arc(-8 + i*3.2, -1 - (Math.cos(i)*1.5), 2.5, 0, Math.PI*2);
            ctx.fill();
        }
    }
}

function drawRockAlpinePlant(ctx, type, rVal) {
    if (type === 'background') {
        // Dwarf conifer (neat small pine tree shape)
        ctx.fillStyle = '#064e3b';
        ctx.beginPath();
        ctx.moveTo(0, -55 - rVal*15);
        ctx.lineTo(-14, -20);
        ctx.lineTo(14, -20);
        ctx.closePath();
        ctx.fill();
        
        ctx.beginPath();
        ctx.moveTo(0, -35);
        ctx.lineTo(-18, 0);
        ctx.lineTo(18, 0);
        ctx.closePath();
        ctx.fill();
    } else if (type === 'midground') {
        // Creeping Sedum / Stonecrop (Succulent leaves, pink highlights)
        ctx.fillStyle = '#475569'; // slate grey stone base
        ctx.beginPath();
        ctx.ellipse(0, 0, 15, 6, 0, 0, Math.PI*2);
        ctx.fill();
        
        // Creeping pink sedum succulent cover
        ctx.fillStyle = '#db2777'; // dark pink
        for (let i = 0; i < 7; i++) {
            ctx.beginPath();
            ctx.arc(-10 + i * 3.5, -4 - (Math.sin(i)*1.5), 3, 0, Math.PI*2);
            ctx.fill();
        }
    } else {
        // Alpine rock cluster and moss
        ctx.fillStyle = '#4b5563'; // Rock grey
        ctx.beginPath();
        ctx.moveTo(-10, 0);
        ctx.lineTo(-4, -8);
        ctx.lineTo(6, -6);
        ctx.lineTo(10, 0);
        ctx.closePath();
        ctx.fill();
        
        // Moss accent
        ctx.fillStyle = '#4d7c0f';
        ctx.beginPath();
        ctx.ellipse(4, -1, 6, 2, 0.1, 0, Math.PI*2);
        ctx.fill();
    }
}

// -------------------------------------------------------------
// GOOGLE PHOTOS CLIENT-SIDE MOCK INTEGRATION
// -------------------------------------------------------------
function initGooglePhotos() {
    // Open Google Photos Modal
    btnGooglePhotos.addEventListener('click', () => {
        modalGooglePhotos.classList.remove('hidden');
    });

    // Close Modal Button
    btnCloseModal.addEventListener('click', () => {
        modalGooglePhotos.classList.add('hidden');
    });

    // Close on overlay click
    modalGooglePhotos.addEventListener('click', (e) => {
        if (e.target === modalGooglePhotos) {
            modalGooglePhotos.classList.add('hidden');
        }
    });

    // Simulate Google Sign-in Connection
    btnGphotosLogin.addEventListener('click', () => {
        btnGphotosLogin.disabled = true;
        btnGphotosLogin.innerHTML = `
            <span class="loading-spinner" style="border-top-color: #3b82f6; width: 14px; height: 14px; margin-right: 8px; display: inline-block; vertical-align: middle;"></span>
            Connecting to Google...
        `;
        
        setTimeout(() => {
            gphotosAuthView.classList.add('hidden');
            gphotosGridView.classList.remove('hidden');
            
            btnGphotosLogin.disabled = false;
            btnGphotosLogin.innerHTML = `Sign in with Google`;
        }, 1100);
    });

    // Google Sign-out Simulation
    btnGphotosLogout.addEventListener('click', () => {
        gphotosGridView.classList.add('hidden');
        gphotosAuthView.classList.remove('hidden');
        // Reset selections
        gphotosItems.forEach(el => el.classList.remove('selected'));
        updateImportButtonState();
    });

    // Photo Selection Toggle
    gphotosItems.forEach(item => {
        item.addEventListener('click', () => {
            item.classList.toggle('selected');
            updateImportButtonState();
        });
    });

    // Import Action Handler
    btnGphotosImport.addEventListener('click', () => {
        const selectedItems = document.querySelectorAll('.gphotos-item.selected');
        let processed = 0;
        
        selectedItems.forEach(item => {
            const src = item.dataset.src;
            const name = item.dataset.name;
            const id = 'img_gphoto_' + Date.now() + '_' + Math.random().toString(36).substr(2, 9);
            
            const imageObject = {
                id: id,
                src: src,
                name: name,
                isDemo: false
            };
            
            state.images.push(imageObject);
            addGalleryItem(imageObject);
            processed++;
            
            if (processed === selectedItems.length) {
                // Select the last imported photo as active workspace photo
                selectWorkspaceImage(id);
                // Clear selection states, close modal
                gphotosItems.forEach(el => el.classList.remove('selected'));
                updateImportButtonState();
                modalGooglePhotos.classList.add('hidden');
            }
        });
    });
}

function updateImportButtonState() {
    const selectedItems = document.querySelectorAll('.gphotos-item.selected');
    btnGphotosImport.textContent = `Import Selected Photos (${selectedItems.length})`;
    btnGphotosImport.disabled = selectedItems.length === 0;
}
