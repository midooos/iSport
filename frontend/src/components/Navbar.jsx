
import { NavLink } from "react-router-dom";

function Navbar() {

    return (
        <header className="navbar">

            <div className="navbar__inner">

                <NavLink to="/" className="navbar__brand">
                    iSport
                </NavLink>

                <nav className="navbar__links">

                    <NavLink to="/" end>
                        Événements
                    </NavLink>

                    <NavLink to="/events/new">
                        Créer un événement
                    </NavLink>

                </nav>

                <div className="navbar__auth">
                </div>

            </div>

        </header>
    );
}

export default Navbar;
