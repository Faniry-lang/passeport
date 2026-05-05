(function () {
    const btnRecherche = document.getElementById("btnRecherchePasseport");
    const champRecherche = document.getElementById("rechercheNumero");
    const messageRecherche = document.getElementById("messageRecherche");
    const typeVisaSelect = document.getElementById("typeVisaSelect");
    const piecesContainer = document.getElementById("piecesContainer");
    const champsContainer = document.getElementById("champsDynamiquesContainer");

    const initialPieceIds = new Set(((window.initialDemandeForm && window.initialDemandeForm.pieceIds) || []).map(String));
    const initialChamps = (window.initialDemandeForm && window.initialDemandeForm.champsDynamiques) || {};

    function setValue(id, value) {
        const element = document.getElementById(id);
        if (element && value !== null && value !== undefined) {
            element.value = String(value);
        }
    }

    function clearMessage() {
        if (messageRecherche) {
            messageRecherche.textContent = "";
        }
    }

    async function rechercherPasseport() {
        clearMessage();

        const numero = (champRecherche && champRecherche.value || "").trim();
        if (!numero) {
            messageRecherche.textContent = "Saisis un numero de passeport.";
            return;
        }

        const response = await fetch(`/demandes/recherche-passeport?numero=${encodeURIComponent(numero)}`);

        if (response.status === 404) {
            messageRecherche.textContent = "Passeport introuvable. Tu peux saisir les infos manuellement.";
            return;
        }

        if (!response.ok) {
            messageRecherche.textContent = "Erreur lors de la recherche du passeport.";
            return;
        }

        const data = await response.json();
        setValue("nom", data.nom);
        setValue("prenom", data.prenom);
        setValue("nomJeuneFille", data.nomJeuneFille);
        setValue("dtn", data.dtn);
        setValue("situationFamilialeId", data.situationFamilialeId);
        setValue("nationaliteId", data.nationaliteId);
        setValue("adresse", data.adresse);
        setValue("email", data.email);
        setValue("telephone", data.telephone);

        setValue("passeportNumero", data.passeportNumero);
        setValue("passeportDateDelivrance", data.passeportDateDelivrance);
        setValue("passeportDateExpiration", data.passeportDateExpiration);

        messageRecherche.textContent = "Passeport trouve et formulaire pre-rempli.";
    }

    function inputTypeFromTypeChamp(typeChamp) {
        const normalise = (typeChamp || "").toLowerCase();
        if (normalise.includes("date")) {
            return "date";
        }
        if (normalise.includes("number") || normalise.includes("nombre") || normalise.includes("numeric")) {
            return "number";
        }
        return "text";
    }

    async function chargerMetaTypeVisa() {
        const typeVisaId = typeVisaSelect && typeVisaSelect.value;
        piecesContainer.innerHTML = "";
        champsContainer.innerHTML = "";

        if (!typeVisaId) {
            return;
        }

        const [piecesResponse, champsResponse] = await Promise.all([
            fetch(`/demandes/type-visa/${encodeURIComponent(typeVisaId)}/pieces`),
            fetch(`/demandes/type-visa/${encodeURIComponent(typeVisaId)}/champs`)
        ]);

        if (piecesResponse.ok) {
            const pieces = await piecesResponse.json();
            pieces.forEach((piece) => {
                const wrapper = document.createElement("label");
                wrapper.className = "field";

                const checkbox = document.createElement("input");
                checkbox.type = "checkbox";
                checkbox.name = "pieceIds";
                checkbox.value = piece.id;
                checkbox.checked = initialPieceIds.has(String(piece.id));

                const text = document.createElement("span");
                text.textContent = piece.obligatoire ? `${piece.nom} (obligatoire)` : piece.nom;

                wrapper.appendChild(checkbox);
                wrapper.appendChild(text);
                piecesContainer.appendChild(wrapper);
            });
        }

        if (champsResponse.ok) {
            const champs = await champsResponse.json();
            champs.forEach((champ) => {
                const wrapper = document.createElement("div");
                wrapper.className = "field";

                const label = document.createElement("label");
                label.textContent = champ.nom;

                const input = document.createElement("input");
                input.type = inputTypeFromTypeChamp(champ.typeChamp);
                input.name = `champsDynamiques[${champ.id}]`;
                const initialValue = initialChamps[champ.id] || initialChamps[String(champ.id)];
                if (initialValue !== undefined && initialValue !== null) {
                    input.value = initialValue;
                }

                wrapper.appendChild(label);
                wrapper.appendChild(input);
                champsContainer.appendChild(wrapper);
            });
        }
    }

    if (btnRecherche) {
        btnRecherche.addEventListener("click", function () {
            rechercherPasseport().catch(() => {
                messageRecherche.textContent = "Erreur reseau pendant la recherche.";
            });
        });
    }

    if (typeVisaSelect) {
        typeVisaSelect.addEventListener("change", function () {
            chargerMetaTypeVisa().catch(() => {
                piecesContainer.innerHTML = "";
                champsContainer.innerHTML = "";
            });
        });

        if (typeVisaSelect.value) {
            chargerMetaTypeVisa().catch(() => {
                piecesContainer.innerHTML = "";
                champsContainer.innerHTML = "";
            });
        }
    }
})();

