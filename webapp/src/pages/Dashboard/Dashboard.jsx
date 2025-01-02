import Header from "../../components/common/Header/Header";
import Sidebar from "../../components/common/Sidebar/Sidebar";
import MainContent from "../../components/common/MainContent/MainContent";

import { useContext, useState } from "react";
import { AuthContext } from '../../contexts/AuthContext';
import { getSidebarOptions } from "../../utils/userOptions";


const Dashboard = () => {

    
    const { user } = useContext(AuthContext);
    const sidebarOptions = getSidebarOptions(user.role);
    const [selectedOption, setSelectedOption] = useState(sidebarOptions[0]);
        
    return(
        <div className="dashboard">
            <Sidebar
                onSelect={(option) => setSelectedOption({
                    title : option.title,
                    component : option.component
                })}
                options={sidebarOptions}
            />
            <div className="page-right">
                <Header
                    title={selectedOption.title}
                    user={{
                        name : user.name + ' ' + user.surname,
                        imgSource : 'icons/profile-picture.svg'
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