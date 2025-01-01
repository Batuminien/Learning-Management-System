import com.example.loginmultiplatform.model.TeacherCourses

data class StudentClassResponse(
    val id: Int,
    val name: String,
    val description: String,
    val teacherCourses: List<TeacherCourses>,
    val studentIdAndNames: Map<String, String>,
    val assignmentIds: List<Int>
)