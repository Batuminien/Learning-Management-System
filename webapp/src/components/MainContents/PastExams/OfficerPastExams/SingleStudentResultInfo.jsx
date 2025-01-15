import { useEffect, useState } from "react";
import useAuth from "../../../../hooks/useAuth";
import { ArrowDown, ArrowUp } from "../../../../../public/icons/Icons";
import ExamResultChart from "../ExamResultChart";

const SingleStudentResultInfo = ({student, index, onChange}) => {
    const { user } = useAuth();
    const [totalNetScore, setTotalNetScore] = useState(0);
    const [isExpanded, setIsExpanded] = useState(false);
    const [canEdit, setCanEdit] = useState(false);

    useEffect(() => {
        if(user.role === 'ROLE_ADMIN') setCanEdit(true);
        setTotalNetScore(student.subjectResults.reduce((total, subject) => total + subject.netScore, 0));
    }, [student]);

    const handleScoreChange = (field, value, subjectId) => {
        const updatedResults = student.subjectResults.map((subject) => {
          if (subject.id === subjectId) {
            return { ...subject, [field]: value };
          }
          return subject;
        });
    
        const updatedStudent = { ...student, subjectResults: updatedResults };
        onChange(updatedStudent);
      };

    return(
        <>
            <div style={{marginBottom : '12px'}}>
                <div className="unit-header" onClick={() => setIsExpanded((prev) => !prev)}>
                    <div className="unit-header-info">
                        <span className='unit-subject'>{index}</span>
                        <span>{student.studentName}</span>
                        <span>Net sayısı : {totalNetScore}</span>
                        {student.subjectResults.map(subject => (
                            <span>{subject.subjectName} : {subject.netScore}</span>
                        ))}
                    </div>
                    {isExpanded ? <ArrowUp/> : <ArrowDown/>}
                </div>
                {isExpanded && (
                    <>
                        <br/>
                        <br/>
                        <table className="student-result-table">
                            <thead>
                                <tr>
                                    <th className="table-header">Konu</th>
                                    <th className="table-header">Doğru</th>
                                    <th className="table-header">Yanlış</th>
                                    <th className="table-header">Boş</th>
                                    <th className="table-header">Net</th>
                                </tr>
                            </thead>
                            <tbody>
                                {student.subjectResults.sort((a, b) => a.subjectName.localeCompare(b.subjectName)).map((category) => (
                                    <tr key={category.id} style={{borderBottom : '2px solid grey'}}>
                                        
                                        <td className="table-row-element">{category.subjectName}</td>
                                        <td className="table-row-element">
                                            <input
                                                className='input'
                                                type="number"
                                                value={category.correctAnswers}
                                                onChange={(event) => 
                                                    handleScoreChange('correctAnswers', parseFloat(event.target.value), category.id)
                                                }
                                                disabled={!canEdit}
                                            />
                                        </td>
                                        <td className="table-row-element">
                                            <input
                                                className='input'
                                                type="number"
                                                value={category.incorrectAnswers}
                                                onChange={(event) => 
                                                    handleScoreChange('incorrectAnswers', parseFloat(event.target.value), category.id)
                                                }
                                                disabled={!canEdit}
                                            />
                                        </td>
                                        <td className="table-row-element">
                                            <input
                                                className='input'
                                                type="number"
                                                value={category.blankAnswers}
                                                onChange={(event) => 
                                                    handleScoreChange('blankAnswers', parseFloat(event.target.value), category.id)
                                                }
                                                disabled={!canEdit}
                                            />
                                        </td>
                                        <td className="table-row-element">
                                            <input
                                                className='input'
                                                type="number"
                                                disabled
                                                defaultValue={category.netScore}
                                            />
                                        </td>
                                    </tr>
                                ))}
                            </tbody>
                        </table>
                        <ExamResultChart subjectResults={student.subjectResults}/>
                    </>
                )}
                <br />
                <div style={{borderBottom : '2px solid grey'}}></div>
            </div>
        </>
    );
}
export default SingleStudentResultInfo;