import { PiExamBold, PiPlusBold } from "react-icons/pi";
import NewExam from "./NewExam";
import PreviousExams from "./PreviousExams";
import { useState } from "react";
import Navigator from "../../../common/Navigator/Navigator";


const OfficerPastExams = ({user}) => {



    const examOptions = [
        {title : 'Eski Sınavlar', component : PreviousExams, iconSource : PiExamBold},
        {title : 'Yeni Sınav', component : NewExam, iconSource : PiPlusBold},
    ];

    const [selectedOption, setSelectedOption] = useState(examOptions[0]);



    return(
        <>
            {user.role === 'ROLE_ADMIN' ? (
                <>
                    <Navigator
                    options={examOptions}
                    onSelect={option => setSelectedOption(option)}
                    />
                    {selectedOption.component && <selectedOption.component/>}
                </>
            ) : (
                <PreviousExams/>
            )}
            
        </>
    );
}
export default OfficerPastExams;