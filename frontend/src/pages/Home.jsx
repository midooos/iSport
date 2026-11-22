import { useEffect, useState } from 'react'
import { getHealth } from '../services/api.js'



function Home() {

    const [status, setStatus] = useState("Vérification en cours...");

    async function verifierBackend() {

        try {
            const data = await getHealth();

            setStatus(data.status);
        }
        catch (error) {
            setStatus("Le back ne répond pas");
        }

    }

    useEffect(() => {
        verifierBackend();
    }, []);

    return (
        <div>
            <h1>iSport</h1>

            <p>Statut du back : {status}</p>
        </div>
    );
}

export default Home;

