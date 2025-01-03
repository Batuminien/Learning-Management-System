import { useState } from "react";
import NewAnnouncement from "./NewAnnouncement";
import OtherAnnouncements from "./OtherAnnouncements";
import Navigator from "../../../common/Navigator/Navigator";

import { PiPlusBold } from "react-icons/pi";
import { PiChatBold } from "react-icons/pi";
import { PiBellRingingBold } from "react-icons/pi";

import CreatedAnnouncements from "./CreatedAnnouncements";

const OfficerAnnouncements = () => {

    const announcementOptions = [
        {title : 'Duyuru Ekle', component : NewAnnouncement, iconSource : PiPlusBold},
        {title : 'Oluşturulan Duyurular', component : CreatedAnnouncements, iconSource : PiChatBold},
        {title : 'Diğer Duyurular', component : OtherAnnouncements, iconSource : PiBellRingingBold}
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