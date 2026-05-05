import { useState, useEffect } from 'react';
import './Tracker.css';

const API_BASE = import.meta.env.VITE_API_BASE_URL || 'http://localhost:8081';

function Tracker() {
  const [numeroPasseport, setNumeroPasseport] = useState('');
  const [demandeId, setDemandeId] = useState('');
  const [demandes, setDemandes] = useState([]);
  const [error, setError] = useState('');
  const [loading, setLoading] = useState(false);
  const [ordre, setOrdre] = useState('desc');
  const [lastSearch, setLastSearch] = useState(null);

  const fetchByPasseport = async (e, numeroParam, ordreParam) => {
    if (e && e.preventDefault) e.preventDefault();
    const numero = numeroParam ?? numeroPasseport;
    const ordreToUse = ordreParam ?? ordre;
    if (!numero) return;

    setLoading(true);
    setError('');
    try {
      setLastSearch({ type: 'passeport', value: numero });
      const res = await fetch(`${API_BASE}/api/demandes/by-passeport?numero=${numero}&ordre=${ordreToUse}`);
      if (!res.ok) throw new Error('Failed to fetch data');
      const data = await res.json();
      setDemandes(data);
    } catch (err) {
      setError(err.message);
      setDemandes([]);
    } finally {
      setLoading(false);
    }
  };

  const fetchByDemandeId = async (e, idParam, ordreParam) => {
    if (e && e.preventDefault) e.preventDefault();
    const id = idParam ?? demandeId;
    const ordreToUse = ordreParam ?? ordre;
    if (!id) return;

    setLoading(true);
    setError('');
    try {
      setLastSearch({ type: 'demande', value: id });
      const res = await fetch(`${API_BASE}/api/demandes/by-demande?id=${id}&ordre=${ordreToUse}`);
      if (!res.ok) throw new Error('Failed to fetch data');
      const data = await res.json();
      setDemandes(data);
    } catch (err) {
      setError(err.message);
      setDemandes([]);
    } finally {
      setLoading(false);
    }
  };

  // On mount: check query params and run a search automatically
  useEffect(() => {
    const params = new URLSearchParams(window.location.search);
    const idParam = params.get('id');
    const numeroParam = params.get('numero') || params.get('numeroPasseport');
    const ordreParam = params.get('ordre');
    if (ordreParam) setOrdre(ordreParam);
    if (idParam) {
      setDemandeId(idParam);
      fetchByDemandeId(null, idParam, ordreParam);
    } else if (numeroParam) {
      setNumeroPasseport(numeroParam);
      fetchByPasseport(null, numeroParam, ordreParam);
    }
  }, []);

  const toggleOrdre = async () => {
    const newOrdre = ordre === 'desc' ? 'asc' : 'desc';
    setOrdre(newOrdre);
    
    if (lastSearch) {
      setLoading(true);
      setError('');
      try {
        let url = '';
        if (lastSearch.type === 'passeport') {
          url = `${API_BASE}/api/demandes/by-passeport?numero=${lastSearch.value}&ordre=${newOrdre}`;
        } else {
          url = `${API_BASE}/api/demandes/by-demande?id=${lastSearch.value}&ordre=${newOrdre}`;
        }
        const res = await fetch(url);
        if (!res.ok) throw new Error('Failed to fetch data');
        const data = await res.json();
        setDemandes(data);
      } catch (err) {
        setError(err.message);
        setDemandes([]);
      } finally {
        setLoading(false);
      }
    }
  };

  return (
    <div className="tracker-container">
      <h2>Suivi des Demandes de Visa</h2>

      <div className="forms-container">
        {/* Formulaire Recherche par Passeport */}
        <form onSubmit={fetchByPasseport} className="search-form">
          <h3>Rechercher par Numéro de Passeport</h3>
          <div className="form-group">
            <input 
              type="text" 
              placeholder="Ex: P1234567" 
              value={numeroPasseport} 
              onChange={(e) => setNumeroPasseport(e.target.value)} 
            />
            <button type="submit" disabled={loading}>Rechercher</button>
          </div>
        </form>

        {/* Formulaire Recherche par ID Demande */}
        <form onSubmit={fetchByDemandeId} className="search-form">
          <h3>Rechercher par ID Demande</h3>
          <div className="form-group">
            <input 
              type="number" 
              placeholder="Ex: 15" 
              value={demandeId} 
              onChange={(e) => setDemandeId(e.target.value)} 
            />
            <button type="submit" disabled={loading}>Rechercher</button>
          </div>
        </form>
      </div>

      {error && <div className="error-message">{error}</div>}

      <div className="results-container">
        {demandes.length > 0 ? (
          <div>
            <div className="results-header" style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
              <h3>Historique des demandes</h3>
              <button onClick={toggleOrdre} className="order-toggle-btn">
                Trier: {ordre === 'desc' ? 'Plus récentes en premier' : 'Plus anciennes en premier'}
              </button>
            </div>
            <div className="timeline">
              {demandes.map((demande, index) => (
                <div key={demande.id || index} className={`timeline-item ${index === 0 ? 'highlight' : ''}`}>
                  <div className="timeline-content">
                    <h4>Demande N° {demande.id}</h4>
                    <p><strong>Passeport :</strong> {demande.passeportNumero}</p>
                    <p><strong>Type de Visa :</strong> {demande.typeVisa}</p>
                    <p><strong>Date demande :</strong> {demande.dateDemande}</p>
                    <div className="status-badge">
                      <strong>Statut Actuel :</strong> {demande.statutActuel || 'Non défini'}
                    </div>
                    {demande.dateStatut && (
                      <p className="status-date"><small>Le {new Date(demande.dateStatut).toLocaleString()}</small></p>
                    )}
                  </div>
                </div>
              ))}
            </div>
          </div>
        ) : (
          !loading && <p>Aucun résultat à afficher.</p>
        )}
      </div>
    </div>
  );
}

export default Tracker;
