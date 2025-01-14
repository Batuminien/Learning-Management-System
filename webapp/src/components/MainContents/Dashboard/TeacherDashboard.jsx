import AnnouncementSummary from "./AnnouncementSummary";




const TeacherDashboard = () => {





    return(
        <div className="dashboard-template">
            <div className="unit-container summary">
                <h2 className="summary-title">Duyurular</h2>
                <div className="summary-content">
                    <AnnouncementSummary/>
                </div>
            </div>
            <div className="unit-container"></div>
            <div className="unit-container"></div>
            <div className="unit-container"></div>
        </div>
    );
}
export default TeacherDashboard;