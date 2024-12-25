import { useContext } from "react";
import { AuthContext } from "../../../contexts/AuthContext";
import StudentAttendance from "./StudentAttendance/StudentAttendance";
import OfficerAttendance from "./OfficerAttendance/OfficerAttendance";




const Attendance = () => {
    const { user } = useContext(AuthContext);

    return(
        <div className="main-content-container">
            {user.role === 'ROLE_STUDENT'
                ? <StudentAttendance />
                : <OfficerAttendance />
            }
        </div>
    );
}
export default Attendance;