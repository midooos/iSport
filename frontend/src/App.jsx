import { Routes, Route, Link } from "react-router-dom";
import Layout from "./components/Layout.jsx";
import ProtectedRoute from "./components/ProtectedRoute.jsx";
import AuthProvider from "./context/AuthProvider.jsx";
import Dashboard from "./pages/Dashboard.jsx";
import CreateEvent from "./pages/CreateEvent.jsx";
import Login from "./pages/Login.jsx";
import Profile from "./pages/Profile.jsx";
import Register from "./pages/Register.jsx";

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
    <AuthProvider>
      <Routes>
        <Route element={<Layout />}>
          <Route index element={<Dashboard />} />
          <Route
            path="events/new"
            element={
              <ProtectedRoute>
                <CreateEvent />
              </ProtectedRoute>
            }
          />
          <Route path="register" element={<Register />} />
          <Route path="login" element={<Login />} />
          <Route
            path="profile"
            element={
              <ProtectedRoute>
                <Profile />
              </ProtectedRoute>
            }
          />
          <Route path="*" element={<NotFound />} />
        </Route>
      </Routes>
    </AuthProvider>
  );
}
