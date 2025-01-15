import { useState } from "react";
import { ArrowDown, ArrowUp } from "../../../../../public/icons/Icons";
import useAuth from "../../../../hooks/useAuth";
import SingleStudentResultInfo from "./SingleStudentResultInfo";
import { deletePastExam, updatePastExam } from "../../../../services/pastExamService";

const SingleOfficerExam = ({exam}) => {
    const { user } = useAuth();
    const [isExpanded, setIsExpanded] = useState(false);
    const [examResults, setExamResults] = useState(exam.results);

    const handleExamDeletion = async () => {
        try{
            const response = await deletePastExam(exam.id);
            console.log(response);
        }catch(error){
            console.log(error);
        }
    }

    const handleExamUpdate = async () => {
        const updatedResults = examResults.map(studentResult => ({
            studentId : studentResult.studentId,
            subjectResults : studentResult.subjectResults.map(subject => ({
                subjectName : subject.subjectName,
                correctAnswers : subject.correctAnswers,
                incorrectAnswers : subject.incorrectAnswers,
                blankAnswers : subject.blankAnswers 
            }))
        }))
        console.log(updatedResults);
        const updatedExam = {
            name : exam.name,
            examType : exam.examType,
            results : updatedResults,
        }
        console.log('updated results : ', updatedExam);
        try{
            const response = await updatePastExam(exam.id, updatedExam);
            console.log(response);
        }catch(error){
            console.log(error);
        }
    }

    const handleResultChange = (updatedStudent) => {
        const updatedResults = examResults.map(student => (
            student.id === updatedStudent.id ? updatedStudent : student
        ));
        setExamResults(updatedResults);
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
                    <br />
                    {examResults.map((student, index) => (
                        <div>
                            <SingleStudentResultInfo
                                student={student}
                                index={index}
                                onChange={handleResultChange}
                                key={student.id}
                            />
                        </div>
                    ))}
                    {user.role === 'ROLE_ADMIN' && (
                        <>
                            <div className="unit-footer">
                                <button className="delete-btn btn" onClick={handleExamDeletion}>Sil</button>
                                <button className="btn" onClick={handleExamUpdate}>Kaydet</button>
                            </div>
                        </>
                    )}
                </div>
            )}
        </div>
    );
}
export default SingleOfficerExam;