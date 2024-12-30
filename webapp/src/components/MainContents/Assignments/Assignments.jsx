import './Assignments.css';

import { useContext } from 'react';
import { AuthContext } from '../../../contexts/AuthContext';

import StudentAssignments from './StudentAssignments/StudentAssignments';
import OfficerAssignments from './OfficerAssignments/OfficerAssignments';

const Assignments = () => {
    const { user } = useContext(AuthContext);
    return(
        <div className="main-content-container">
            {user.role === 'ROLE_STUDENT'
                ? <StudentAssignments user={user}/>
                : <OfficerAssignments user={user}/>
            }
        </div>
    );
}
export default Assignments;