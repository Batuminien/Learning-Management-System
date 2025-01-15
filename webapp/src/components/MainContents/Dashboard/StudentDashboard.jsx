import AnnouncementSummary from './AnnouncementSummary';
import AssignmentSummary from './AssignmentSummary';
import PastExamSummary from './PastExamSummary';



const StudentDashBoard = () => {





    return(
        <div className="dashboard-template">
            <div className="unit-container summary">
                <h2 className="summary-title">Duyurular</h2>
                <div className="summary-content">
                    <AnnouncementSummary/>
                </div>
            </div>
            
            <div className="unit-container summary">
                <h2 className="summary-title">Geçmiş Sınavlar</h2>
                <div className="summary-content">
                    <PastExamSummary/>
                </div>
            </div>
            
            <div className="unit-container"></div>
            
            <div className="unit-container summary">
                <h2 className="summary-title">Ödevler</h2>
                <div className="summary-content">
                    <AssignmentSummary/>
                </div>
            </div>
        </div>
    );
}
export default StudentDashBoard;