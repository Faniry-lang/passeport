(function () {
    const form = document.getElementById("duplicataForm");
    if (!form) return;

    const idInput = document.getElementById("idDemandeRecherche");
    const btnRecherche = document.getElementById("btnRecherche");
    const btnModeManuel = document.getElementById("btnModeManuel");
    const messageBox = document.getElementById("messageBox");
    const serverMessage = document.getElementById("serverMessage");
    const resultatRecherche = document.getElementById("resultatRecherche");
    const sectionManuelle = document.getElementById("sectionManuelle");
    const searchPasseport = document.getElementById("searchPasseport");
    const btnSearchPasseport = document.getElementById("btnSearchPasseport");

    const identiteFields = [
        "nom", "prenom", "nomJeuneFille", "dtn", "situationFamilialeId", "nationaliteId",
        "adresse", "email", "telephone", "passeportDateDelivrance", "passeportDateExpiration",
        "typeVisaId", "dateEntree", "lieuEntree"
    ].map((id) => document.getElementById(id));

    function showMessage(text, ok) {
        if (!messageBox) return;
        messageBox.classList.remove("d-none", "alert-danger", "alert-success", "alert-info");
        messageBox.classList.add(ok ? "alert-success" : "alert-danger");
        messageBox.textContent = text;
    }

    function toggleModeManuelle(manuel) {
        if (manuel) {
            sectionManuelle.classList.remove("collapse");
            identiteFields.forEach(f => { if (f) f.required = true; });
        } else {
            sectionManuelle.classList.add("collapse");
            identiteFields.forEach(f => { if (f) f.required = false; });
        }
    }

    function lockIdentite(lock) {
        identiteFields.forEach((f) => {
            if (f) f.readOnly = lock;
            if (f && f.tagName === "SELECT") f.disabled = lock;
        });
        if (searchPasseport) searchPasseport.readOnly = lock;
        if (btnSearchPasseport) btnSearchPasseport.disabled = lock;
    }

    function setField(id, value) {
        const el = document.getElementById(id);
        if (!el || value === null || value === undefined) return;
        el.value = String(value);
    }

    async function rechercherDemande() {
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

        toggleModeManuelle(false);
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
        resultatRecherche.textContent = `Demande #${d.demandeId || idDemande} chargee. Mode demande existante.`;
        showMessage(data.message || "Recherche terminee.", true);
    }

    async function rechercherPasseportData() {
        const numero = (searchPasseport.value || "").trim();
        if (!numero) {
            showMessage("Saisissez un numéro de passeport.", false);
            return;
        }

        try {
            const formData = new FormData();
            formData.append("numero", numero);
            const response = await fetch(`/demandes/recherche-passeport?numero=${encodeURIComponent(numero)}`);

            if (response.ok) {
                const data = await response.json();
                setField("nom", data.demandeur.nom);
                setField("prenom", data.demandeur.prenom);
                setField("nomJeuneFille", data.demandeur.nomJeuneFille);
                setField("dtn", data.demandeur.dtn);
                setField("situationFamilialeId", data.demandeur.situationFamiliale.id);
                setField("nationaliteId", data.demandeur.nationalite.id);
                setField("adresse", data.demandeur.adresse);
                setField("email", data.demandeur.email);
                setField("telephone", data.demandeur.telephone);
                setField("passeportDateDelivrance", data.dateDelivrance);
                setField("passeportDateExpiration", data.dateExpiration);

                // On met à jour aussi le passeportNumero du bloc Duplicata
                const passeportDuplicataChamp = document.getElementById("passeportNumero");
                if (passeportDuplicataChamp) setField("passeportNumero", numero);

                showMessage("Passeport trouvé. Identité pré-remplie.", true);
            } else {
                showMessage("Passeport non trouvé.", false);
            }
        } catch (e) {
            showMessage("Erreur réseau.", false);
        }
    }

    btnRecherche.addEventListener("click", function () {
        rechercherDemande().catch(() => showMessage("Erreur reseau pendant la recherche.", false));
    });

    if (btnSearchPasseport) {
        btnSearchPasseport.addEventListener("click", rechercherPasseportData);
    }

    btnModeManuel.addEventListener("click", function () {
        idInput.value = "";
        toggleModeManuelle(true);
        lockIdentite(false);
        resultatRecherche.textContent = "Mode manuel actif. Completer l'identite complete requise.";
        showMessage("Mode creation manuelle active.", true);
    });

    function validationMinimale() {
        return form.checkValidity();
    }

    async function soumettreAjax(event) {
        event.preventDefault();
        if (!validationMinimale()) {
            form.reportValidity();
            return;
        }

        const formData = new FormData(form);
        const payload = {
            idDemande: formData.get("idDemandeRecherche") || null,
            carteResidentId: formData.get("carteResidentId") || null,
            passeportNumero: formData.get("passeportNumero"),
            motif: formData.get("motif"),
            demande: {
                nom: formData.get("nom"),
                prenom: formData.get("prenom"),
                nomJeuneFille: formData.get("nomJeuneFille"),
                dtn: formData.get("dtn"),
                situationFamilialeId: formData.get("situationFamilialeId"),
                nationaliteId: formData.get("nationaliteId"),
                adresse: formData.get("adresse"),
                email: formData.get("email"),
                telephone: formData.get("telephone"),
                passeportNumero: formData.get("passeportNumero"),
                passeportDateDelivrance: formData.get("passeportDateDelivrance"),
                passeportDateExpiration: formData.get("passeportDateExpiration"),
                typeVisaId: formData.get("typeVisaId"),
                dateEntree: formData.get("dateEntree"),
                lieuEntree: formData.get("lieuEntree"),
                visaReference: formData.get("visaReferenceOptionnel"),
                visaDateDelivrance: formData.get("visaDateDelivrance"),
                visaDateExpiration: formData.get("visaDateExpiration")
            }
        };

        const response = await fetch(form.action, {
            method: "POST",
            headers: { "Content-Type": "application/json" },
            body: JSON.stringify(payload)
        });
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

    form.addEventListener("submit", function (event) {
        soumettreAjax(event).catch(() => showMessage("Erreur reseau pendant la soumission.", false));
    });

    if (serverMessage && serverMessage.textContent) {
        showMessage(serverMessage.textContent, true);
    }
})();

