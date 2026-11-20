import { Routes, Route, Link } from "react-router-dom";
import Layout from "./components/Layout.jsx";
import Dashboard from "./pages/Dashboard.jsx";
import CreateEvent from "./pages/CreateEvent.jsx";

function NotFound() {
  return (
    <>
      <h1>Page introuvable</h1>
      <p>Cette adresse n'existe pas. Retourne à la liste des événements.</p>
      <Link to="/">Voir les événements</Link>
    </>
  );
}

export default function App() {
  return (
    <Routes>
      <Route element={<Layout />}>
        <Route index element={<Dashboard />} />
        <Route path="events/new" element={<CreateEvent />} />
        <Route path="*" element={<NotFound />} />
      </Route>
    </Routes>
  );
}
