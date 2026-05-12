(function () {
    const ENFORCE_CAPTURE_REQUIRED = true; // si true, on bloque la soumission si photo ou signature manquantes

    // Simple SignaturePad fallback (very small implementation) if no external lib is available
    function SimpleSignaturePad(canvas) {
        this.canvas = canvas;
        this.ctx = canvas.getContext('2d');
        this.drawing = false;
        this._resize();
        window.addEventListener('resize', () => this._resize());
        this._bindEvents();
    }

    SimpleSignaturePad.prototype._resize = function () {
        const ratio = Math.max(window.devicePixelRatio || 1, 1);
        const w = this.canvas.clientWidth;
        const h = this.canvas.clientHeight;
        this.canvas.width = w * ratio;
        this.canvas.height = h * ratio;
        this.ctx.scale(ratio, ratio);
        this.clear();
    };

    SimpleSignaturePad.prototype._bindEvents = function () {
        this.canvas.addEventListener('pointerdown', (e) => {
            this.drawing = true;
            this.ctx.beginPath();
            this.ctx.moveTo(e.offsetX, e.offsetY);
        });
        this.canvas.addEventListener('pointermove', (e) => {
            if (!this.drawing) return;
            this.ctx.lineTo(e.offsetX, e.offsetY);
            this.ctx.strokeStyle = '#000';
            this.ctx.lineWidth = 2;
            this.ctx.lineCap = 'round';
            this.ctx.lineJoin = 'round';
            this.ctx.stroke();
        });
        ['pointerup', 'pointerleave'].forEach(evt => {
            this.canvas.addEventListener(evt, () => {
                this.drawing = false;
            });
        });
    };

    SimpleSignaturePad.prototype.clear = function () {
        this.ctx.clearRect(0, 0, this.canvas.width, this.canvas.height);
    };

    SimpleSignaturePad.prototype.isEmpty = function () {
        const blank = document.createElement('canvas');
        blank.width = this.canvas.width;
        blank.height = this.canvas.height;
        return this.canvas.toDataURL() === blank.toDataURL();
    };

    SimpleSignaturePad.prototype.toDataURL = function (type, quality) {
        return this.canvas.toDataURL(type || 'image/png', quality || 0.9);
    };

    // Main controller
    const state = {
        stream: null,
        signaturePad: null,
        photoTaken: false
    };

    function init() {
        const modalEl = document.getElementById('captureMediaModal');
        if (!modalEl) return; // nothing to do

        const video = document.getElementById('cameraPreview');
        const photoCanvas = document.getElementById('photoCanvas');
        const startBtn = document.getElementById('startCameraBtn');
        const takeBtn = document.getElementById('takePhotoBtn');
        const retakeBtn = document.getElementById('retakePhotoBtn');
        const cameraStatus = document.getElementById('cameraStatus');
        const saveBtn = document.getElementById('saveCaptureBtn');
        const clearSigBtn = document.getElementById('clearSignatureBtn');
        const signatureCanvas = document.getElementById('signaturePad');

        // init signature pad
        state.signaturePad = new SimpleSignaturePad(signatureCanvas);

        function startCamera() {
            cameraStatus.textContent = '';
            if (state.stream) return;
            navigator.mediaDevices.getUserMedia({ video: true })
                .then(s => {
                    state.stream = s;
                    video.srcObject = s;
                    video.play();
                    startBtn.disabled = true;
                })
                .catch(err => {
                    console.error(err);
                    cameraStatus.textContent = 'Accès caméra refusé ou non disponible.';
                    showFlashModal('Erreur caméra', 'Impossible d\'accéder à la caméra : ' + (err.message || err));
                });
        }

        function stopCamera() {
            if (!state.stream) return;
            state.stream.getTracks().forEach(t => t.stop());
            state.stream = null;
            try { video.srcObject = null; } catch (e) { }
            startBtn.disabled = false;
        }

        function takePhoto() {
            if (!state.stream && !video.srcObject) {
                showFlashModal('Photo', 'La caméra n\'est pas activée.');
                return;
            }
            const w = video.videoWidth || video.clientWidth;
            const h = video.videoHeight || video.clientHeight || 240;
            photoCanvas.width = w;
            photoCanvas.height = h;
            const ctx = photoCanvas.getContext('2d');
            ctx.drawImage(video, 0, 0, w, h);
            // show canvas and hide video preview
            photoCanvas.style.display = 'block';
            video.style.display = 'none';
            state.photoTaken = true;
            retakeBtn.style.display = '';
            takeBtn.disabled = true;
            stopCamera(); // stop after capture to free camera
        }

        function retakePhoto() {
            photoCanvas.style.display = 'none';
            video.style.display = '';
            state.photoTaken = false;
            retakeBtn.style.display = 'none';
            takeBtn.disabled = false;
            startCamera();
        }

        function saveAndClose() {
            // serialize photo
            const photoInput = document.getElementById('photoBase64');
            const signatureInput = document.getElementById('signatureBase64');

            if (state.photoTaken) {
                const dataUrl = photoCanvas.toDataURL('image/jpeg', 0.8);
                photoInput.value = dataUrl;
            } else {
                photoInput.value = '';
            }

            // signature
            if (!state.signaturePad.isEmpty()) {
                signatureInput.value = state.signaturePad.toDataURL('image/png');
            } else {
                signatureInput.value = '';
            }

            // close modal
            const modal = bootstrap.Modal.getInstance(document.getElementById('captureMediaModal'));
            if (modal) modal.hide();
            stopCamera();
            showFlashModal('Capture enregistrée', 'Photo et signature ajoutées au formulaire (si fournies).', false);
        }

        startBtn.addEventListener('click', startCamera);
        takeBtn.addEventListener('click', takePhoto);
        retakeBtn.addEventListener('click', retakePhoto);
        clearSigBtn.addEventListener('click', function () { state.signaturePad.clear(); });
        saveBtn.addEventListener('click', saveAndClose);

        // when modal hides, ensure camera is stopped
        const captureModalEl = document.getElementById('captureMediaModal');
        captureModalEl.addEventListener('hidden.bs.modal', () => {
            stopCamera();
        });

        // attach submit handler to ensure images are attached (already attached on save),
        // and optionally block submit if we require photo/signature.
        const form = document.getElementById('demandeForm');
        if (form) {
            form.addEventListener('submit', function (e) {
                if (!ENFORCE_CAPTURE_REQUIRED) return;
                const photoVal = document.getElementById('photoBase64')?.value || '';
                const sigVal = document.getElementById('signatureBase64')?.value || '';
                if (!photoVal || !sigVal) {
                    e.preventDefault();
                    showFlashModal('Erreur', 'La photo et la signature sont requises avant la soumission.');
                }
            });
        }
    }

    // util pour afficher le modal flash existant
    function showFlashModal(title, message, isError = true) {
        const modalTitle = document.getElementById('flashModalLabel');
        const modalBody = document.getElementById('flashModalBody');
        modalTitle.textContent = title;
        modalBody.innerHTML = message.replace(/\n/g, '<br>');
        if (isError) {
            modalTitle.classList.remove('text-success');
            modalTitle.classList.add('text-danger');
        } else {
            modalTitle.classList.remove('text-danger');
            modalTitle.classList.add('text-success');
        }
        const flashModal = new bootstrap.Modal(document.getElementById('flashModal'));
        flashModal.show();
    }

    // init après DOM ready
    if (document.readyState === 'loading') {
        document.addEventListener('DOMContentLoaded', init);
    } else {
        init();
    }
})();
