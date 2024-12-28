
import NewUser from "../components/MainContents/NewUser/NewUser";
import Assignments from '../components/MainContents/Assignments/Assignments';
import Attendance from "../components/MainContents/Attendance/Attendance";



import { PiExamBold } from "react-icons/pi";
import { PiStudentBold } from "react-icons/pi";
import { PiMegaphoneBold } from "react-icons/pi";
import { PiBookOpenBold } from "react-icons/pi";
import { PiHouseBold } from "react-icons/pi";
import { PiUserPlusBold } from "react-icons/pi";
import { PiClipboardTextBold } from "react-icons/pi";

const userOptionsMap = {
    ROLE_ADMIN: [
        { title: 'Ana Menü', component: null, iconSource : PiHouseBold },
        { title: 'Öğrenci Bul', component: null, iconSource : PiStudentBold },
        { title: 'Ödev Takibi', component: Assignments, iconSource : PiBookOpenBold },
        { title: 'Yoklama', component: Attendance, iconSource : PiClipboardTextBold },
        { title: 'Sınav Sonuçları', component: null, iconSource : PiExamBold },
        { title: 'Duyurular', component: null, iconSource : PiMegaphoneBold },
        { title: 'Yeni Kullanıcı', component: NewUser, iconSource : PiUserPlusBold }
    ],
    ROLE_TEACHER: [
        { title: 'Ana Menü', component: null, iconSource : PiHouseBold },
        { title: 'Öğrenci Bul', component: null, iconSource : PiStudentBold },
        { title: 'Ödev Takibi', component: Assignments, iconSource : PiBookOpenBold },
        { title: 'Yoklama', component: Attendance, iconSource : PiClipboardTextBold },
        { title: 'Sınav Sonuçları', component: null, iconSource : PiExamBold },
        { title: 'Duyurular', component: null, iconSource : PiMegaphoneBold }
    ],
    ROLE_COORDINATOR: [
        { title: 'Ana Menü', component: null, iconSource : PiHouseBold },
        { title: 'Öğrenci Bul', component: null, iconSource : PiStudentBold },
        { title: 'Ödev Takibi', component: Assignments, iconSource : PiBookOpenBold },
        { title: 'Yoklama', component: Attendance, iconSource : PiClipboardTextBold },
        { title: 'Sınav Sonuçları', component: null, iconSource : PiExamBold },
        { title: 'Duyurular', component: null, iconSource : PiMegaphoneBold }
    ],
    ROLE_STUDENT: [
        { title: 'Ana Menü', component: null, iconSource : PiHouseBold },
        { title: 'Ödevler', component: Assignments, iconSource : PiBookOpenBold },
        { title: 'Yoklama', component: Attendance, iconSource : PiClipboardTextBold },
        { title: 'Geçmiş Sınavlar', component: null, iconSource : PiExamBold },
        { title: 'Duyurular', component: null, iconSource : PiMegaphoneBold }
    ]
}

export const getSidebarOptions = (role) => {
    return userOptionsMap[role];
}