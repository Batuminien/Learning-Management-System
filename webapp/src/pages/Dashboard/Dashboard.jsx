import Header from "../../components/common/Header/Header";
import Sidebar from "../../components/common/Sidebar/Sidebar";
import MainContent from "../../components/common/MainContent/MainContent";

import './Dashboard.css'

import { useContext, useState } from "react";
import { AuthContext } from '../../contexts/AuthContext';
import { getSidebarOptions } from "../../utils/userOptions";


const Dashboard = () => {

    const [selectedOption, setSelectedOption] = useState({
        title : 'Ana Menü',
        component : null
    });
    
    const { user } = useContext(AuthContext);
    const sidebarOptions = getSidebarOptions(user.role);
        
    return(
        <div className="dashboard">
            <Sidebar
                onSelect={(option) => setSelectedOption(option)}
                options={sidebarOptions}
            />
            <div className="page-right">
                <Header 
                    title={selectedOption.title}
                    user={{
                        name : user.username,
                        imgSource : '../../../public/icons/profile-picture.svg'
                    }}
                />
                <MainContent 
                    content={selectedOption.component}
                />
            </div>
        </div>
    );

}
export default Dashboard