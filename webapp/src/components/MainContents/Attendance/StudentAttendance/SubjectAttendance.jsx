import { useState } from 'react';

const SubjectAttendance = ({ stats, history, forceExpand }) => {
    const [isExpanded, setIsExpanded] = useState(false);

    return(
        <div className="assignment-container">
            <div className="assignment-header" onClick={() => setIsExpanded((prev) => !prev)}>
                <div className="assignment-header-info">
                    <img src="https://placeholder.pics/svg/32x32" alt="icon" />
                    <span className="assignment-subject">{stats.courseName}</span>
                </div>
                {!forceExpand && (
                    <button className="expand-btn">
                        <img src={(isExpanded || forceExpand) ? 'icons/arrow-up.svg' : 'icons/arrow-down.svg'} alt="toggle assignment details" />
                    </button>
                )}
            </div>
            {(isExpanded || forceExpand) && 
                (history ?                
                    (
                        <div className="assignment-body">
                            <div style={{border : '1px solid grey'}}></div>
                            <div className="assignment-body-section">
                                <table className="attendance-table">
                                    <thead className='table-header'>
                                        <tr>
                                            <th className="table-header">Tarih</th>
                                            <th className="table-header">Yoklama Durumu</th>
                                            <th className="table-header">Yoklama Notu</th>
                                        </tr>
                                    </thead>
                                    <tbody>
                                        {history.map(attendance => (
                                            <tr className="table-row" key={attendance.attendanceId}>
                                                <td className="table-row-element">
                                                    {(new Date(attendance.date)).toLocaleDateString('en-GB')}
                                                </td>
                                                <td className="table-row-element">
                                                    {attendance.status}
                                                </td>
                                                <td className="table-row-element">
                                                    {attendance.comment ? attendance.comment : '-'}
                                                </td>
                                            </tr>
                                        ))}
                                    </tbody>
                                </table>
                            </div>
                            <div style={{border : '1px solid grey'}}></div>
                            <div className="assignment-body-section">
                                <table className="attendance-table">
                                    <thead className='table-header'>
                                        <tr>
                                            <th className="table-header">Katıldığı dersler</th>
                                            <th className="table-header">Geç kaldığı dersler</th>
                                            <th className="table-header">Gelmediği dersler</th>
                                            <th className="table-header">Katılım Yüzdesi</th>
                                        </tr>
                                    </thead>
                                    <tbody>
                                        <tr className="table-row">
                                            <th className="table-row-element">{stats.presentCount}</th>
                                            <th className="table-row-element">{stats.lateCount}</th>
                                            <th className="table-row-element">{stats.absentCount}</th>
                                            <th className="table-row-element">{stats.attendancePercentage}%</th>
                                        </tr>
                                    </tbody>
                                </table>
                            </div>
                        </div>
                    ) : (
                        <i>Bu derse ait yoklama kaydı bulunamadı.</i>
                    )
                )
            }
        </div>
    );
}
export default SubjectAttendance;