(function () {
    const form = document.getElementById("duplicataForm");
    if (!form) return;

    const idInput = document.getElementById("idDemandeRecherche");
    const btnRecherche = document.getElementById("btnRecherche");
    const btnModeManuel = document.getElementById("btnModeManuel");
    const messageBox = document.getElementById("messageBox");
    const serverMessage = document.getElementById("serverMessage");
    const resultatRecherche = document.getElementById("resultatRecherche");

    const identiteFields = [
        "nom", "prenom", "nomJeuneFille", "dtn", "situationFamilialeId", "nationaliteId"
    ].map((id) => document.getElementById(id));

    function showMessage(text, ok) {
        if (!messageBox) return;
        messageBox.classList.remove("d-none", "alert-danger", "alert-success", "alert-info");
        messageBox.classList.add(ok ? "alert-success" : "alert-danger");
        messageBox.textContent = text;
    }

    function lockIdentite(lock) {
        identiteFields.forEach((f) => {
            if (f) f.readOnly = lock;
            if (f && f.tagName === "SELECT") f.disabled = lock;
        });
    }

    function setField(id, value) {
        const el = document.getElementById(id);
        if (!el || value === null || value === undefined) return;
        el.value = String(value);
    }

    async function rechercher() {
        const idDemande = (idInput.value || "").trim();
        if (!idDemande) {
            showMessage("Saisir un id_demande avant la recherche.", false);
            return;
        }

        const url = `/demandes/duplicata/recherche?idDemande=${encodeURIComponent(idDemande)}`;
        const response = await fetch(url);
        const data = await response.json().catch(() => ({}));

        if (!response.ok || !data.success) {
            lockIdentite(false);
            resultatRecherche.textContent = data.message || "Demande introuvable.";
            showMessage(data.message || "Recherche en erreur.", false);
            return;
        }

        const d = data.data || {};
        setField("idDemandeRecherche", d.demandeId || idDemande);
        setField("nom", d.nom);
        setField("prenom", d.prenom);
        setField("nomJeuneFille", d.nomJeuneFille);
        setField("dtn", d.dtn);
        setField("situationFamilialeId", d.situationFamilialeId);
        setField("nationaliteId", d.nationaliteId);
        setField("passeportNumero", d.passeportNumero);
        setField("carteResidentId", d.carteResidentId);

        lockIdentite(true);
        resultatRecherche.textContent = `Demande #${d.demandeId || idDemande} chargee. Identite verrouillee.`;
        showMessage(data.message || "Recherche terminee.", true);
    }

    function validationMinimale() {
        const motif = document.getElementById("motif").value;
        if (motif !== "PERTE" && motif !== "DETERIORATION") {
            showMessage("Motif invalide. Choisir PERTE ou DETERIORATION.", false);
            return false;
        }
        return form.checkValidity();
    }

    async function soumettreAjax(event) {
        event.preventDefault();
        if (!validationMinimale()) {
            form.reportValidity();
            return;
        }

        const payload = new FormData(form);
        const response = await fetch(form.action, { method: "POST", body: payload });
        const data = await response.json().catch(() => null);

        if (data && data.success) {
            const message = encodeURIComponent(data.message || "Operation reussie");
            const demandeId = data.data && data.data.demandeId ? `&demandeId=${encodeURIComponent(data.data.demandeId)}` : "";
            window.location.href = `/demandes/confirmation?message=${message}${demandeId}`;
            return;
        }

        if (!response.ok) {
            showMessage((data && data.message) || "Echec de soumission.", false);
            return;
        }

        // Fallback si backend renvoie une redirection HTML classique.
        window.location.href = "/demandes/confirmation";
    }

    btnRecherche.addEventListener("click", function () {
        rechercher().catch(() => showMessage("Erreur reseau pendant la recherche.", false));
    });

    btnModeManuel.addEventListener("click", function () {
        lockIdentite(false);
        resultatRecherche.textContent = "Mode manuel actif. Completer tous les champs obligatoires.";
        showMessage("Mode creation manuelle active.", true);
    });

    form.addEventListener("submit", function (event) {
        soumettreAjax(event).catch(() => showMessage("Erreur reseau pendant la soumission.", false));
    });

    if (serverMessage && serverMessage.textContent) {
        showMessage(serverMessage.textContent, true);
    }
})();

