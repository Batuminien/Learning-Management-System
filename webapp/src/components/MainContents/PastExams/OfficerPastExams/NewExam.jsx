import Select from "react-select";
import { examTypeOptions } from "./OfficerPastExams";
import { useState } from "react";
import { fetchCssVariable } from "../../../../utils/fileUtils";
import Document from "../../../common/Document";


const NewExam = () => {

    const [examTitle, setExamTitle] = useState('');
    const [titleError, setTitleError] = useState('');

    const [selectedType, setSelectedType] = useState({label : '', value : null});
    const [typeError, setTypeError] = useState('');

    const [uploaded, setUploaded] = useState(false);
    const [resultFile, setResultFile] = useState(null);

    const handleFileUpload = (event) => {
        const file = event.target.files[0];
        
        if (file) {
            setResultFile(file);
            setUploaded(true);
          console.log('File selected:', file);
        }
    }


    return(
        <div className="newAssignmentForm">
            <div className="input-container">
                <span className="label">Sınav Başlığı</span>
                <input
                    type="text"
                    className="input"
                    value={examTitle}
                    onChange={(e) => {setExamTitle(e.target.value), setTitleError()}}
                />
                {titleError && <p className="error-message">{titleError}</p>}
            </div>
            <div className="input-container">
                <span className="label">Sınav Türü</span>
                <Select
                    styles={{
                        control: (base) => ({
                            ...base,
                            backgroundColor : fetchCssVariable('--input-background')
                        })
                    }}
                    options={examTypeOptions}
                    closeMenuOnSelect
                    value={selectedType}
                    onChange={selected => {setSelectedType(selected), setTypeError('')}}
                />
                {typeError && <p className="error-message">{typeError}</p>}
            </div>
            <div className='input-container'>
                <span className="label">Sınav sonuçları</span>
                {uploaded ? (
                    <Document
                        file={resultFile}
                        isRemovable={true}
                        onRemove={() => {setNewPhoto(null), setUploaded(false)}}
                    />
                ) : (
                    <>
                        <input
                            type="file"
                            className='input'
                            onChange={handleFileUpload}
                        />
                    </>
                )}
            </div>
        </div>
    );
}
export default NewExam;