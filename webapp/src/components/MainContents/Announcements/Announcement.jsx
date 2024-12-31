import { useContext } from "react";
import { AuthContext } from "../../../contexts/AuthContext";

import OfficerAnnouncements from "./OfficerAnnouncemets";
import StudentAnnouncements from "./StudentAnnouncements";




const Announcements = () => {
    const { user } = useContext(AuthContext);

    return(
        <div className="main-content-container">
            {user.role === 'ROLE_STUDENT'
                ? <StudentAnnouncements/>
                : <OfficerAnnouncements/>
            }
        </div>
    );
}
export default Announcements;