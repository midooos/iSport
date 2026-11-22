// Toutes les requêtes vers le back passent par ce fichier

/*export async function getHealth() {
  const response = await fetch('/api/health')
  if (!response.ok) {
    throw new Error('Le back ne répond pas')
  }
  return response.json()
*/

export async function getHealth() {

    const response = await fetch("/api/health");

    if (response.ok === true) {
        const data = await response.json();
        return data;
    } 
    else {
        throw new Error("Le back ne répond pas");
    }

}


