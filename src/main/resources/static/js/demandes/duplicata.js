(function () {
    const form = document.getElementById("duplicataForm");
    if (!form) return;

    const idInput = document.getElementById("idDemandeRecherche");
    const btnRecherche = document.getElementById("btnRecherche");
    const btnModeManuel = document.getElementById("btnModeManuel");
    const btnCarteResident = document.getElementById("btnCarteResident");
    const messageBox = document.getElementById("messageBox");
    const serverMessage = document.getElementById("serverMessage");
    const resultatRecherche = document.getElementById("resultatRecherche");
    const sectionManuelle = document.getElementById("sectionManuelle");
    const carteResidentForm = document.getElementById("carteResidentForm");
    const btnCreerCarteResident = document.getElementById("btnCreerCarteResident");
    const searchPasseport = document.getElementById("searchPasseport");
    const btnSearchPasseport = document.getElementById("btnSearchPasseport");

    const identiteFields = [
        "nom", "prenom", "nomJeuneFille", "dtn", "situationFamilialeId", "nationaliteId",
        "adresse", "email", "telephone", "passeportDateDelivrance", "passeportDateExpiration",
        "typeVisaId", "dateEntree", "lieuEntree"
    ].map((id) => document.getElementById(id));

    const carteResidentFields = [
        "carteResidentDateDebut", "carteResidentDateFin"
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

    function toggleCarteResidentForm(show) {
        if (show) {
            carteResidentForm.classList.remove("collapse");
            carteResidentFields.forEach(f => { if (f) f.required = true; });
        } else {
            carteResidentForm.classList.add("collapse");
            carteResidentFields.forEach(f => { if (f) f.required = false; });
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

    function clearCarteResidentForm() {
        carteResidentFields.forEach(f => { if (f) f.value = ""; });
    }

    function validateCarteResidentForm() {
        const dateDebut = document.getElementById("carteResidentDateDebut").value;
        const dateFin = document.getElementById("carteResidentDateFin").value;

        // Validation des champs de la carte résident
        if (!dateDebut || !dateFin) {
            showMessage("Les dates de début et fin de validité sont obligatoires.", false);
            return false;
        }

        if (new Date(dateFin) <= new Date(dateDebut)) {
            showMessage("La date de fin doit être postérieure à la date de début.", false);
            return false;
        }

        // Validation des champs d'identité requis pour la création de carte résident
        const nom = document.getElementById("nom").value.trim();
        const prenom = document.getElementById("prenom").value.trim();
        const dtn = document.getElementById("dtn").value;
        const situationFamilialeId = document.getElementById("situationFamilialeId").value;
        const nationaliteId = document.getElementById("nationaliteId").value;
        const adresse = document.getElementById("adresse").value.trim();

        if (!nom || !prenom || !dtn || !situationFamilialeId || !nationaliteId || !adresse) {
            showMessage("Les informations d'identité (nom, prénom, date de naissance, situation familiale, nationalité, adresse) sont obligatoires pour créer une carte résident.", false);
            return false;
        }

        return true;
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
        toggleCarteResidentForm(false);
        lockIdentite(false);
        clearCarteResidentForm();
        resultatRecherche.textContent = "Mode manuel actif. Completer l'identite complete requise.";
        showMessage("Mode creation manuelle active.", true);
    });

    if (btnCarteResident) {
        btnCarteResident.addEventListener("click", function () {
            idInput.value = "";
            toggleModeManuelle(true); // Activer aussi le formulaire d'identité
            toggleCarteResidentForm(true);
            lockIdentite(false);
            clearCarteResidentForm();
            resultatRecherche.textContent = "Mode carte résident actif. Remplissez l'identité puis créez la carte résident pour obtenir l'ID.";
            showMessage("Mode création carte résident active. Remplissez d'abord les informations d'identité.", true);
        });
    }

    async function creerCarteResident() {
        if (!validateCarteResidentForm()) {
            return;
        }

        const formData = {
            dateDebut: document.getElementById("carteResidentDateDebut").value,
            dateFin: document.getElementById("carteResidentDateFin").value,
            demandeur: {
                nom: document.getElementById("nom").value.trim(),
                prenom: document.getElementById("prenom").value.trim(),
                nomJeuneFille: document.getElementById("nomJeuneFille").value.trim(),
                dtn: document.getElementById("dtn").value,
                situationFamilialeId: document.getElementById("situationFamilialeId").value,
                nationaliteId: document.getElementById("nationaliteId").value,
                adresse: document.getElementById("adresse").value.trim(),
                email: document.getElementById("email").value.trim(),
                telephone: document.getElementById("telephone").value.trim()
            },
            passeportNumero: document.getElementById("passeportNumero").value.trim()
        };

        try {
            const response = await fetch("/api/cartes-resident/create", {
                method: "POST",
                headers: { "Content-Type": "application/json" },
                body: JSON.stringify(formData)
            });

            const data = await response.json().catch(() => null);

            if (response.ok && data && data.success) {
                const carteResidentId = data.data.id;
                setField("carteResidentId", carteResidentId);

                // Pré-remplir aussi les informations du demandeur depuis la réponse
                if (data.data.demandeur) {
                    setField("nom", data.data.demandeur.nom);
                    setField("prenom", data.data.demandeur.prenom);
                    setField("nomJeuneFille", data.data.demandeur.nomJeuneFille);
                    setField("dtn", data.data.demandeur.dtn);
                    setField("situationFamilialeId", data.data.demandeur.situationFamilialeId);
                    setField("nationaliteId", data.data.demandeur.nationaliteId);
                    setField("adresse", data.data.demandeur.adresse);
                    setField("email", data.data.demandeur.email);
                    setField("telephone", data.data.demandeur.telephone);
                }

                showMessage(`Carte résident créée avec succès (ID: ${carteResidentId}). L'ID a été assigné automatiquement.`, true);
                resultatRecherche.textContent = `Carte résident #${carteResidentId} créée. Vous pouvez maintenant remplir les informations de duplicata.`;

                // Fermer le formulaire de carte résident après création
                setTimeout(() => {
                    toggleCarteResidentForm(false);
                    // Garder le mode manuel actif pour permettre la saisie des autres informations
                }, 2000);
            } else {
                showMessage((data && data.message) || "Erreur lors de la création de la carte résident.", false);
            }
        } catch (e) {
            showMessage("Erreur réseau lors de la création de la carte résident.", false);
        }
    }

    if (btnCreerCarteResident) {
        btnCreerCarteResident.addEventListener("click", creerCarteResident);
    }

    function validationMinimale() {
        // Vérifier si le formulaire de carte résident est visible
        const isCarteResidentVisible = !carteResidentForm.classList.contains("collapse");

        if (isCarteResidentVisible) {
            // Si le formulaire de carte résident est visible, valider ce formulaire
            return validateCarteResidentForm() && form.checkValidity();
        }

        // Sinon, validation normale du formulaire
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

