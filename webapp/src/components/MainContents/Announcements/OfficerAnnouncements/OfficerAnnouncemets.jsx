import { useState } from "react";
import NewAnnouncement from "./NewAnnouncement";
import PastAnnouncements from "./PastAnnouncements";
import Navigator from "../../../common/Navigator/Navigator";

import { PiPlusBold } from "react-icons/pi";
import { PiClockBold } from "react-icons/pi";

const OfficerAnnouncements = () => {

    const announcementOptions = [
        {title : 'Duyuru Ekle', component : NewAnnouncement, iconSource : PiPlusBold},
        {title : 'Geçmiş Duyurular', component : PastAnnouncements, iconSource : PiClockBold}
    ]
    const [selectedOption, setSelectedOption] = useState(announcementOptions[0]);


    return(
        <>
            <Navigator
                options={announcementOptions}
                onSelect={(option) => setSelectedOption(option)}
            />
            {selectedOption.component && <selectedOption.component/>}
        </>
    );
}
export default OfficerAnnouncements;