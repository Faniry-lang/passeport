(function () {
    const form = document.getElementById("duplicataForm");
    if (!form) return;

    let carteResidentAutoCreationMode = false;

    const idInput = document.getElementById("idDemandeRecherche");
    const btnRecherche = document.getElementById("btnRecherche");
    const btnModeManuel = document.getElementById("btnModeManuel");
    const btnCarteResident = document.getElementById("btnCarteResident");
    const messageBox = document.getElementById("messageBox");
    const serverMessage = document.getElementById("serverMessage");
    const resultatRecherche = document.getElementById("resultatRecherche");
    const sectionManuelle = document.getElementById("sectionManuelle");
    const carteResidentForm = document.getElementById("carteResidentForm");
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

    function setContainerFieldsEnabled(container, enabled) {
        if (!container) return;
        const fields = container.querySelectorAll("input, select, textarea, button");
        fields.forEach((field) => {
            if (!field) return;
            if (field.id === "btnRecherche" || field.id === "btnModeManuel" || field.id === "btnCarteResident") {
                return;
            }
            if (enabled) {
                if (field.dataset && field.dataset.wasRequired === "true") {
                    field.required = true;
                }
                field.disabled = false;
            } else {
                if (field.required) {
                    field.dataset.wasRequired = "true";
                }
                field.required = false;
                field.disabled = true;
            }
        });
    }

    function showMessage(text, ok) {
        if (!messageBox) return;
        messageBox.classList.remove("d-none", "alert-danger", "alert-success", "alert-info");
        messageBox.classList.add(ok ? "alert-success" : "alert-danger");
        messageBox.textContent = text;
    }

    function toggleModeManuelle(manuel) {
        if (manuel) {
            sectionManuelle.classList.remove("collapse");
            setContainerFieldsEnabled(sectionManuelle, true);
            identiteFields.forEach(f => { if (f) f.required = true; });
        } else {
            sectionManuelle.classList.add("collapse");
            setContainerFieldsEnabled(sectionManuelle, false);
            identiteFields.forEach(f => { if (f) f.required = false; });
        }
    }

    function toggleCarteResidentForm(show) {
        if (show) {
            carteResidentForm.classList.remove("collapse");
            setContainerFieldsEnabled(carteResidentForm, true);
            carteResidentFields.forEach(f => { if (f) f.required = true; });
            carteResidentAutoCreationMode = true;
            // When creating the carte resident automatically, the "carteResidentId" field
            // (which is the target of the automatic creation) must not be required.
            const carteResidentIdField = document.getElementById("carteResidentId");
            if (carteResidentIdField) {
                carteResidentIdField.dataset.wasRequired = carteResidentIdField.required ? "true" : "false";
                carteResidentIdField.required = false;
            }
        } else {
            carteResidentForm.classList.add("collapse");
            setContainerFieldsEnabled(carteResidentForm, false);
            carteResidentFields.forEach(f => { if (f) f.required = false; });
            carteResidentAutoCreationMode = false;
            const carteResidentIdField = document.getElementById("carteResidentId");
            if (carteResidentIdField) {
                // restore previous required state if it was required before
                if (carteResidentIdField.dataset && carteResidentIdField.dataset.wasRequired === "true") {
                    carteResidentIdField.required = true;
                }
            }
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

        if (!dateDebut || !dateFin) {
            showMessage("Les dates de début et fin de validité sont obligatoires.", false);
            return false;
        }

        if (new Date(dateFin) <= new Date(dateDebut)) {
            showMessage("La date de fin doit être postérieure à la date de début.", false);
            return false;
        }

        const nom = document.getElementById("nom").value.trim();
        const prenom = document.getElementById("prenom").value.trim();
        const dtn = document.getElementById("dtn").value;
        const situationFamilialeId = document.getElementById("situationFamilialeId").value;
        const nationaliteId = document.getElementById("nationaliteId").value;
        const adresse = document.getElementById("adresse").value.trim();

        if (!nom || !prenom || !dtn || !situationFamilialeId || !nationaliteId || !adresse) {
            showMessage("Les informations d'identité sont obligatoires pour créer une carte résident.", false);
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
            const response = await fetch(`/demandes/recherche-passeport?numero=${encodeURIComponent(numero)}`);

            if (response.ok) {
                const data = await response.json();
                // Support two shapes returned by endpoints:
                // 1) { demandeur: { ... }, dateDelivrance, dateExpiration }
                // 2) PasseportRechercheDto with top-level fields (nom, prenom, ...)
                const demandeur = data.demandeur ? data.demandeur : {
                    nom: data.nom,
                    prenom: data.prenom,
                    nomJeuneFille: data.nomJeuneFille,
                    dtn: data.dtn,
                    situationFamiliale: { id: data.situationFamilialeId },
                    nationalite: { id: data.nationaliteId },
                    adresse: data.adresse,
                    email: data.email,
                    telephone: data.telephone
                };

                setField("nom", demandeur.nom);
                setField("prenom", demandeur.prenom);
                setField("nomJeuneFille", demandeur.nomJeuneFille);
                setField("dtn", demandeur.dtn);
                setField("situationFamilialeId", demandeur.situationFamiliale && demandeur.situationFamiliale.id ? demandeur.situationFamiliale.id : data.situationFamilialeId);
                setField("nationaliteId", demandeur.nationalite && demandeur.nationalite.id ? demandeur.nationalite.id : data.nationaliteId);
                setField("adresse", demandeur.adresse);
                setField("email", demandeur.email);
                setField("telephone", demandeur.telephone);
                setField("passeportDateDelivrance", data.dateDelivrance || data.passeportDateDelivrance);
                setField("passeportDateExpiration", data.dateExpiration || data.passeportDateExpiration);
                setField("passeportNumero", numero);

                showMessage("Passeport trouvé. Identité pré-remplie.", true);
            } else {
                showMessage("Passeport non trouvé.", false);
            }
        } catch (e) {
            console.log(e);
            showMessage("Erreur réseau.", false);
        }
    }

    async function createCarteResidentAutomatique(formData) {
        const payload = {
            dateDebut: formData.get("carteResidentDateDebut"),
            dateFin: formData.get("carteResidentDateFin"),
            passeportNumero: formData.get("passeportNumero"),
            demandeur: {
                nom: formData.get("nom"),
                prenom: formData.get("prenom"),
                nomJeuneFille: formData.get("nomJeuneFille"),
                dtn: formData.get("dtn"),
                situationFamilialeId: Number(formData.get("situationFamilialeId") || 0),
                nationaliteId: Number(formData.get("nationaliteId") || 0),
                adresse: formData.get("adresse"),
                email: formData.get("email"),
                telephone: formData.get("telephone")
            }
        };

        const response = await fetch("/api/cartes-resident/create", {
            method: "POST",
            headers: { "Content-Type": "application/json" },
            body: JSON.stringify(payload)
        });

        const data = await response.json().catch(() => null);
        if (!response.ok || !data || data.success === false) {
            throw new Error((data && data.message) || "Erreur lors de la création de la carte résident.");
        }

        const carteResidentId = data?.data?.id;
        if (!carteResidentId) {
            throw new Error("Création carte résident réussie mais ID manquant.");
        }

        setField("carteResidentId", carteResidentId);
        resultatRecherche.textContent = `Carte résident #${carteResidentId} créée automatiquement.`;
        showMessage("Carte résident créée automatiquement.", true);
        return carteResidentId;
    }

    function validationMinimale() {
        const isCarteResidentVisible = !carteResidentForm.classList.contains("collapse");
        if (isCarteResidentVisible) {
            return validateCarteResidentForm() && form.checkValidity();
        }
        return form.checkValidity();
    }

    async function soumettreAjax(event) {
        event.preventDefault();
        if (!validationMinimale()) {
            form.reportValidity();
            return;
        }

        const formData = new FormData(form);

        if (carteResidentAutoCreationMode && !(String(formData.get("carteResidentId") || "").trim())) {
            await createCarteResidentAutomatique(formData);
            formData.set("carteResidentId", document.getElementById("carteResidentId").value);
        }

        const payload = {
            idDemande: formData.get("idDemande") || null,
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

        window.location.href = "/demandes/confirmation";
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
            toggleModeManuelle(true);
            toggleCarteResidentForm(true);
            lockIdentite(false);
            clearCarteResidentForm();
            resultatRecherche.textContent = "Mode carte résident actif. Le submit créera automatiquement la carte résident puis le duplicata.";
            showMessage("Mode carte résident activé. Le submit gérera la création automatique.", true);
        });
    }

    form.addEventListener("submit", function (event) {
        soumettreAjax(event).catch((error) => {
            showMessage(error.message || "Erreur reseau pendant la soumission.", false);
        });
    });

    if (serverMessage && serverMessage.textContent) {
        showMessage(serverMessage.textContent, true);
    }

    // Etat initial: aucune section optionnelle ne doit bloquer la validation native.
    toggleModeManuelle(false);
    toggleCarteResidentForm(false);
})();
