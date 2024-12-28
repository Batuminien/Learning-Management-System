import { useState } from 'react';

import { ArrowDown } from '../../../../../public/icons/Icons';
import { ArrowUp } from '../../../../../public/icons/Icons';

const SubjectAttendance = ({ stats, history, forceExpand }) => {
    const [isExpanded, setIsExpanded] = useState(false);

    return(
        <div className="unit-container">
            <div className="unit-header" onClick={() => setIsExpanded((prev) => !prev)}>
                <div className="unit-header-info">
                    <img src="https://placeholder.pics/svg/32x32" alt="icon" />
                    <span className="unit-subject">{stats.courseName}</span>
                </div>
                {!forceExpand && (
                    <button className="expand-btn">
                        {(isExpanded || forceExpand) ? <ArrowUp/> : <ArrowDown/>}
                    </button>
                )}
            </div>
            {(isExpanded || forceExpand) && 
                (history ?                
                    (
                        <div className="unit-body">
                            <div style={{border : '1px solid grey'}}></div>
                            <div className="unit-body-section">
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
                            <div className="unit-body-section">
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