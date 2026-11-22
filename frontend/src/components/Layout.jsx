

import { Outlet } from "react-router-dom";
import Navbar from "./Navbar.jsx";

function Layout() {

    return (
        <div>

            <a className="skip-link" href="#contenu">
                Aller au contenu
            </a>

            <Navbar />

            <main id="contenu" className="page">
                <Outlet />
            </main>

        </div>
    );
}

export default Layout;

