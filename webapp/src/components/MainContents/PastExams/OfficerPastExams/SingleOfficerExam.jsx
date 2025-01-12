import { useContext, useState } from "react";
import { ArrowDown, ArrowUp } from "../../../../../public/icons/Icons";
import { AuthContext } from "../../../../contexts/AuthContext";



const SingleOfficerExam = ({exam}) => {
    const { user } = useContext(AuthContext);

    const [isExpanded, setIsExpanded] = useState(false);

    const handleExamDeletion = () => {
        console.log('going to delete the exam : ', exam.name);
    }

    return(
        <div className="unit-container">
            <div className="unit-header" onClick={() => setIsExpanded((prev) => !prev)}>
                <div className="unit-header-info">
                    <img src='https://placeholder.pics/svg/32x32' alt='icon' />
                    <span className="unit-subject">{exam.examType}</span>
                    <span className="unit-title">{exam.name}</span>
                    <span className="unit-title">{new Date(exam.date).toLocaleDateString('en-GB')}</span>
                </div>
                <button className="expand-btn">
                    {isExpanded ? <ArrowUp/> : <ArrowDown/>}
                </button>
            </div>
            {isExpanded && (
                <div className="unit-body">
                    <div style={{border : '1px solid grey'}}></div>
                    {exam.results.map(student => (
                        <p>{student.studentName}</p>
                    ))}
                    {user.role === 'ROLE_ADMIN' && (
                        <>
                            <div style={{border : '1px solid grey'}}></div>
                            <div className="unit-footer">
                                <button className="delete-btn btn" onClick={handleExamDeletion}>Sil</button>
                            </div>
                        </>
                    )}
                </div>
            )}
        </div>
    );
}
export default SingleOfficerExam;