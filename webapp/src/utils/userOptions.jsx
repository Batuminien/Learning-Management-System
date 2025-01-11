
import NewUser from "../components/MainContents/NewUser/NewUser";
import Assignments from '../components/MainContents/Assignments/Assignments';
import Attendance from "../components/MainContents/Attendance/Attendance";
import Settings from "../components/MainContents/Settings/Settings";



import { PiExamBold, PiGearBold } from "react-icons/pi";
import { PiStudentBold } from "react-icons/pi";
import { PiMegaphoneBold } from "react-icons/pi";
import { PiBookOpenBold } from "react-icons/pi";
import { PiHouseBold } from "react-icons/pi";
import { PiUserPlusBold } from "react-icons/pi";
import { PiClipboardTextBold } from "react-icons/pi";
import Announcements from "../components/MainContents/Announcements/Announcement";
import StudentDashBoard from "../components/MainContents/Dashboard/StudentDashboard";
import PastExams from "../components/MainContents/PastExams/PastExams";

const userOptionsMap = {
    ROLE_ADMIN: [
        { title: 'Ana Menü', component: null, iconSource : PiHouseBold },
        { title: 'Öğrenci Bul', component: null, iconSource : PiStudentBold },
        { title: 'Ödev Takibi', component: Assignments, iconSource : PiBookOpenBold },
        { title: 'Yoklama', component: Attendance, iconSource : PiClipboardTextBold },
        { title: 'Sınav Sonuçları', component: PastExams, iconSource : PiExamBold },
        { title: 'Duyurular', component: Announcements, iconSource : PiMegaphoneBold },
        { title: 'Yeni Kullanıcı', component: NewUser, iconSource : PiUserPlusBold },
        { title: 'Ayarlar', component: Settings, iconSource : PiGearBold}
    ],
    ROLE_TEACHER: [
        { title: 'Ana Menü', component: null, iconSource : PiHouseBold },
        { title: 'Öğrenci Bul', component: null, iconSource : PiStudentBold },
        { title: 'Ödev Takibi', component: Assignments, iconSource : PiBookOpenBold },
        { title: 'Yoklama', component: Attendance, iconSource : PiClipboardTextBold },
        { title: 'Sınav Sonuçları', component: PastExams, iconSource : PiExamBold },
        { title: 'Duyurular', component: Announcements, iconSource : PiMegaphoneBold },
        { title: 'Ayarlar', component: Settings, iconSource : PiGearBold}
    ],
    ROLE_COORDINATOR: [
        { title: 'Ana Menü', component: null, iconSource : PiHouseBold },
        { title: 'Öğrenci Bul', component: null, iconSource : PiStudentBold },
        { title: 'Ödev Takibi', component: Assignments, iconSource : PiBookOpenBold },
        { title: 'Yoklama', component: Attendance, iconSource : PiClipboardTextBold },
        { title: 'Sınav Sonuçları', component: PastExams, iconSource : PiExamBold },
        { title: 'Duyurular', component: Announcements, iconSource : PiMegaphoneBold },
        { title: 'Ayarlar', component: Settings, iconSource : PiGearBold}
    ],
    ROLE_STUDENT: [
        { title: 'Ana Menü', component: StudentDashBoard, iconSource : PiHouseBold },
        { title: 'Ödevler', component: Assignments, iconSource : PiBookOpenBold },
        { title: 'Yoklama', component: Attendance, iconSource : PiClipboardTextBold },
        { title: 'Geçmiş Sınavlar', component: PastExams, iconSource : PiExamBold },
        { title: 'Duyurular', component: Announcements, iconSource : PiMegaphoneBold },
        { title: 'Ayarlar', component: Settings, iconSource : PiGearBold}
    ]
}

export const getSidebarOptions = (role) => {
    return userOptionsMap[role];
}