import InputField from "../../common/InputField";
import DateInput from "../../common/DateInput";
import ClassesDropdown from "../../common/ClassesDropdown";

import { useContext, useState } from "react";
import { AuthContext } from "../../../contexts/AuthContext";

import { generateRandomPassword, isValidEmail, isValidName, isValidPhoneNumber, isValidTC } from "./NewUserUtils";

import authService from "../../../services/authService";
import { updateStudentInfo } from "../../../services/studentService";
import { addStudent } from "../../../services/classesService";

const NewStudentForm = ({onCreationSuccess, onSubmit, onCreationError}) => {
    const { user } = useContext(AuthContext);

    const [name, setName] = useState('');
    const [nameError, setNameError] = useState('');

    const [surname, setSurname] = useState('');
    const [surnameError, setSurnameError] = useState('');

    const [TC, setTC] = useState('');
    const [TcError, setTcError] = useState('');

    const [birthDate, setBirthDate] = useState('');
    const [birthDateError, setBirthDateError] = useState('');

    const [mailAddress, setMailAddress] = useState('');
    const [mailError, setMailError] = useState('');

    const [phoneNumber, setPhoneNumber] = useState('');
    const [phoneNumberError, setPhoneNumberError] = useState('');

    const [selectedClass, setSelectedClass] = useState({name : '', id : null});
    const [classError, setClassError] = useState('');

    const [parentName, setParentName] = useState('');
    const [parentNameError, setParentNameError] = useState('');

    const [parentSurname, setParentSurname] = useState('');
    const [parentSurnameError, setParentSurnameError] = useState('');

    const [parentPhone, setParentPhone] = useState('');
    const [parentPhoneNumberError, setParentPhoneNumberError] = useState('');

    
    const createStudent = async () => {
        onSubmit();
        let hasError = false;
        if(!isValidName(name)) {setNameError('Geçersiz ad'), hasError=true}
        if(!isValidName(surname)) {setSurnameError('Geçersiz soyad'), hasError=true}
        if(!isValidTC(TC)) {setTcError('Geçersiz TC kimlik numarası'), hasError=true}
        if(!isValidEmail(mailAddress)) {setMailError('Geçersiz e-posta adresi'), hasError=true}
        if(!isValidPhoneNumber(phoneNumber)) {setPhoneNumberError('Numara 0 ile başlamalı ve 11 haneli olmalıdır'), hasError=true}
        if(selectedClass.name === '') {setClassError('Sınıf seçilmedi'), hasError=true}
        if(birthDate === '') {setBirthDateError('Doğum tarihi seçiniz'), hasError=true}
        if(!isValidName(parentName)) {setParentNameError('Geçersiz ad'), hasError=true}
        if(!isValidName(parentSurname)) {setParentSurnameError('Geçersiz soyad'), hasError=true}
        if(!isValidPhoneNumber(parentPhone)) {setParentPhoneNumberError('Numara 0 ile başlamalı ve 11 haneli olmalıdır'), hasError=true}
        if(hasError) return;

        const registerPayload = {
            firstName : name.charAt(0).toUpperCase() + name.slice(1),
            lastName  : surname.charAt(0).toUpperCase() + surname.slice(1),
            email : mailAddress,
            role : 'ROLE_STUDENT',
            username : name + '.' + surname,
            password : generateRandomPassword(),
        }
        try {
            const creationResponse = await authService.register(registerPayload);
            console.log(creationResponse);
            const classResponse = await addStudent(creationResponse.data.userId, Number(selectedClass.id), user.accessToken);
            console.log(classResponse);
            const updatePayload = {
                email : registerPayload.email,
                firstName : registerPayload.firstName,
                lastName : registerPayload.lastName,
                password : registerPayload.password,
                phone : phoneNumber,
                tc : TC,
                birthDate : birthDate,
                registrationDate : new Date().toISOString().slice(0, 10),
                parentName : parentName.charAt(0) + parentName.slice(1) + ' ' + parentSurname.charAt(0) + parentSurname.slice(1),
                parentPhone : parentPhone,
                classId : Number(selectedClass.id),
            }
            const additionalInfoResponse = await updateStudentInfo(creationResponse.data.userId ,updatePayload, user.accessToken);
            onCreationSuccess(registerPayload.password);
        }catch(error) {
            console.log(error.response.data.message);
            onCreationError(error.response.data.message);
        }
    }

    return(
        <div className='form'>
            <div className="form-title">Öğrenci</div>
            <div className='input-fields'>
                <InputField 
                    label={'Adı'}
                    value={name}
                    onChange={(event) => {setName(event.target.value), setNameError('')}}
                    errorMessage={nameError}
                />
                <InputField 
                    label={'Soyadı'}
                    value={surname}
                    onChange={(event) => {setSurname(event.target.value), setSurnameError('')}}
                    errorMessage={surnameError}
                />
                <InputField 
                    label={'TC Kimlik Numarası'}
                    value={TC}
                    onChange={(event) => {setTC(event.target.value), setTcError('')}}
                    errorMessage={TcError}
                />
                <DateInput
                    title={'Doğum Tarihi'}
                    onInput={(date) => setBirthDate(date)}
                    errorMessage={birthDateError}
                />
                <InputField 
                    label={'E-posta Adresi'}
                    value={mailAddress}
                    onChange={(event) => {setMailAddress(event.target.value), setMailError('')}}
                    errorMessage={mailError}
                />
                <InputField
                    label={'Telefon Numarası'}
                    placeholder={'05xx xxx xx xx'}
                    value={phoneNumber}
                    onChange={(event) => {setPhoneNumber(event.target.value), setPhoneNumberError('')}}
                    errorMessage={phoneNumberError}
                />
                <ClassesDropdown
                    user={user}
                    onClassChange={(selectedClass) => {setSelectedClass(selectedClass), setClassError('')}}
                    classErrorMessage={classError}
                />
            </div>

            <div className="form-title">Veli</div>
            <div className="input-fields">
                <InputField
                    label={'Adı'}
                    value={parentName}
                    onChange={(event) => {setParentName(event.target.value), setParentNameError('')}}
                    errorMessage={parentNameError}
                    />
                <InputField
                    label={'Soyadı'}
                    value={parentSurname}
                    onChange={(event) => {setParentSurname(event.target.value), setParentSurnameError('')}}
                    errorMessage={parentSurnameError}
                />
                <InputField
                    label={'Telefon Numarası'}
                    placeholder={'05xx xxx xx xx'}
                    value={parentPhone}
                    onChange={(event) => {setParentPhone(event.target.value), setParentPhoneNumberError('')}}
                    errorMessage={parentPhoneNumberError}
                />
            </div>

            <button type='submit' className='save-btn btn' onClick={createStudent}>Kaydet</button>
        </div>
    );
}
export default NewStudentForm;