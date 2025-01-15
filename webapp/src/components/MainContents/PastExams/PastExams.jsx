import StundentPastExams from "./StudentPastExams";
import OfficerPastExams from "./OfficerPastExams/OfficerPastExams";
import useAuth from "../../../hooks/useAuth";

const PastExams = () => {
    const { user } = useAuth();
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