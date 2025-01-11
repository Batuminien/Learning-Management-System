import { useEffect, useState } from "react";
import { ArrowDown, ArrowUp } from "../../../../public/icons/Icons";
import ExamResultChart from "./ExamResultChart";




const SingleExam = ({exam}) => {


    const [isExpanded, setIsExpanded] = useState(false);
   

    console.log(exam);

    return(
        <div className="unit-container">
            <div className="unit-header" onClick={() => setIsExpanded((prev) => !prev)}>
                <div className="unit-header-info">
                    <img src='https://placeholder.pics/svg/32x32' alt='icon' />
                    <span className="unit-subject">{exam.pastExam.examType}</span>
                    <span className="unit-title">{exam.pastExam.name}</span>
                    <span className="unit-title">{new Date(exam.pastExam.date).toLocaleDateString('en-GB')}</span>
                </div>
                <button className="expand-btn">
                    {isExpanded ? <ArrowUp/> : <ArrowDown/>}
                </button>
            </div>
            {isExpanded && (
                <div className="unit-body">
                    <div style={{border : '1px solid grey'}}></div>
                    <div className='exam-result-content'>        
                        <ExamResultChart subjectResults={exam.subjectResults}/>
                        <table className='exam-result-table'>
                            <thead className="table-header">
                                <tr>
                                    <th className="table-header">Doğru</th>
                                    <th className="table-header">Yanlış</th>
                                    <th className="table-header">Boş</th>
                                    <th className="table-header">Net</th>
                                </tr>
                            </thead>
                            <tbody>
                                {exam.subjectResults.sort((a, b) => a.subjectName.localeCompare(b.subjectName)).map((category) => (
                                    <tr key={category.id} style={{borderBottom : '2px solid grey'}}>
                                        <td className="table-row-element">{category.correctAnswers}</td>
                                        <td className="table-row-element">{category.incorrectAnswers}</td>
                                        <td className="table-row-element">{category.blankAnswers}</td>
                                        <td className="table-row-element">{category.netScore}</td>
                                    </tr>
                                ))}
                            </tbody>
                        </table>

                    </div>
                    <div style={{border : '1px solid grey'}}></div>
                    <div className="unit-footer">
                        <span className="unit-subject">Net sayısı : {exam.subjectResults.reduce((sum, subject) => sum + subject.netScore, 0)}</span>
                        <span className="unit-subject">Genel Net sayısı : {exam.pastExam.overallAverage}</span>
                    </div>
                </div>
            )}
        </div>
    );
}
export default SingleExam;