import { useContext } from "react";
import { AuthContext } from "../../../contexts/AuthContext";
import StundentPastExams from "./StudentPastExams";
import OfficerPastExams from "./OfficerPastExams";


const PastExams = () => {
    const { user } = useContext(AuthContext);
    return(
        <div className="main-content-container">
            {user.role === 'ROLE_STUDENT'
                ? <StundentPastExams user={user}/>
                : <OfficerPastExams  user={user}/>
            }
        </div>
    );
}
export default PastExams;