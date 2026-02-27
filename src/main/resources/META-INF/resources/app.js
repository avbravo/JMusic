// Three.js 3D Background
let scene, camera, renderer, particles;

function init3D() {
    scene = new THREE.Scene();
    camera = new THREE.PerspectiveCamera(75, window.innerWidth / window.innerHeight, 0.1, 1000);
    renderer = new THREE.WebGLRenderer({ antialias: true, alpha: true });
    renderer.setSize(window.innerWidth, window.innerHeight);
    document.getElementById('canvas-container').appendChild(renderer.domElement);

    // Create particles
    const geometry = new THREE.BufferGeometry();
    const vertices = [];
    for (let i = 0; i < 5000; i++) {
        vertices.push(
            Math.random() * 2000 - 1000,
            Math.random() * 2000 - 1000,
            Math.random() * 2000 - 1000
        );
    }
    geometry.setAttribute('position', new THREE.Float32BufferAttribute(vertices, 3));
    const material = new THREE.PointsMaterial({ color: 0x00d2ff, size: 2, transparent: true, opacity: 0.5 });
    particles = new THREE.Points(geometry, material);
    scene.add(particles);

    camera.position.z = 500;

    animate();
}

function animate() {
    requestAnimationFrame(animate);
    particles.rotation.x += 0.0005;
    particles.rotation.y += 0.001;
    renderer.render(scene, camera);
}

window.addEventListener('resize', () => {
    camera.aspect = window.innerWidth / window.innerHeight;
    camera.updateProjectionMatrix();
    renderer.setSize(window.innerWidth, window.innerHeight);
});

// Application Logic
const editor = document.getElementById('lyric-editor');
const metricsContent = document.getElementById('metrics-content');
const rhymesContent = document.getElementById('rhymes-content');
const aiContent = document.getElementById('ai-content');
const songListUl = document.getElementById('songs-ul');
const songNameDisplay = document.getElementById('song-name-display');

let currentSongName = "Nueva Canción";
let debounceTimer;

editor.addEventListener('input', () => {
    clearTimeout(debounceTimer);
    debounceTimer = setTimeout(analyzeLyrics, 500);
});

async function analyzeLyrics() {
    const text = editor.value;
    if (!text) {
        metricsContent.innerHTML = "";
        rhymesContent.innerHTML = "";
        aiContent.innerHTML = "";
        return;
    }

    try {
        const response = await fetch('/songs/analyze', {
            method: 'POST',
            headers: { 'Content-Type': 'text/plain' },
            body: text
        });
        const results = await response.json();
        updateUI(results);
    } catch (e) {
        console.error("Error analyzing:", e);
    }
}

function updateUI(verses) {
    metricsContent.innerHTML = "";
    rhymesContent.innerHTML = "";
    aiContent.innerHTML = "";

    let totalSinalefas = 0;
    let totalLines = 0;

    verses.forEach((verse) => {
        const verseContainer = document.createElement('div');
        verseContainer.className = 'verse-group';
        verseContainer.innerHTML = `<div class="verse-label">ESTROFA ${verse.verseNumber}</div>`;

        verse.lines.forEach((line, lIndex) => {
            totalLines++;
            if (line.hasSinalefa) totalSinalefas++;

            // Metrics Line
            const mLine = document.createElement('div');
            mLine.className = 'line-stat';
            let sinalefaBadge = line.hasSinalefa ? '<span class="badge sinalefa">S</span>' : '';
            let rhymeTag = line.rhymeTag ? `<span class="rhyme-marker color-${line.rhymeTag}">${line.rhymeTag}</span>` : '';

            mLine.innerHTML = `
                <div class="line-metrics">
                    <span class="line-num">${lIndex + 1}</span>
                    <span class="value">${line.syllables}</span>
                    ${sinalefaBadge}
                    ${rhymeTag}
                </div>
            `;
            verseContainer.appendChild(mLine);

            // Rhymes Suggestions
            if (lIndex === verse.lines.length - 1) {
                const rDiv = document.createElement('div');
                rDiv.className = 'verse-stat rhyme-sugg';
                rDiv.innerHTML = `<span class="label">Sugerencia Estrofa ${verse.verseNumber}:</span> <i>${line.suggestions.join(', ')}</i>`;
                rhymesContent.appendChild(rDiv);
            }
        });

        metricsContent.appendChild(verseContainer);
    });

    // AI Feedback
    aiContent.innerHTML = `<div class="verse-stat">
        <span class="value">Revisor IA:</span> ${verses.length} estrofas, ${totalLines} versos. 
        Detecté ${totalSinalefas} sinalefas. 
        ${totalLines > 8 ? "Buena extensión compositiva." : "Considera desarrollar más la estructura."}
    </div>`;
}

// Custom Modal System
const modalOverlay = document.getElementById('modal-overlay');
const modalMessage = document.getElementById('modal-message');
const modalInput = document.getElementById('modal-input');
const modalOkBtn = document.getElementById('modal-ok-btn');
const modalCancelBtn = document.getElementById('modal-cancel-btn');

function showModal(message, isPrompt = false, defaultValue = "") {
    return new Promise((resolve) => {
        modalMessage.textContent = message;
        modalInput.value = defaultValue;
        modalInput.style.display = isPrompt ? 'block' : 'none';
        modalCancelBtn.style.display = isPrompt ? 'inline-block' : 'none';

        modalOverlay.classList.add('active');

        const onOk = () => {
            cleanup();
            resolve(isPrompt ? modalInput.value : true);
        };

        const onCancel = () => {
            cleanup();
            resolve(null);
        };

        const cleanup = () => {
            modalOkBtn.removeEventListener('click', onOk);
            modalCancelBtn.removeEventListener('click', onCancel);
            modalOverlay.classList.remove('active');
        };

        modalOkBtn.addEventListener('click', onOk);
        modalCancelBtn.addEventListener('click', onCancel);
    });
}

// Persistencia
document.getElementById('save-btn').addEventListener('click', async () => {
    const name = await showModal("Nombre de la canción:", true, currentSongName);
    if (!name) return;

    try {
        await fetch('/songs', {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({ name: name, content: editor.value })
        });
        await showModal("¡Canción guardada con éxito!");
        loadSongs();
    } catch (e) {
        await showModal("Error al guardar la canción.");
    }
});

async function loadSongs() {
    try {
        const response = await fetch('/songs');
        const songs = await response.json();
        songListUl.innerHTML = "";
        songs.forEach(song => {
            const li = document.createElement('li');
            li.textContent = song;
            li.onclick = () => loadSong(song);
            songListUl.appendChild(li);
        });
    } catch (e) { }
}

async function loadSong(name) {
    try {
        const response = await fetch(`/songs/${name}`);
        const content = await response.json();
        editor.value = content;
        currentSongName = name;
        songNameDisplay.textContent = name;
        analyzeLyrics();
    } catch (e) { }
}

document.getElementById('new-song-btn').onclick = () => {
    editor.value = "";
    currentSongName = "Nueva Cancion";
    songNameDisplay.textContent = currentSongName;
    updateUI([]);
};

// Start
init3D();
loadSongs();
