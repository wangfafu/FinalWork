package controller.student;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.serializer.SerializerFeature;
import domain.Student;
import domain.Teacher;
import service.StudentService;
import util.JSONUtil;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.sql.SQLException;
import java.util.Collection;

/**
 * 将所有方法组织在一个Controller(Servlet)中
 */
@WebServlet("/student.ctl")
public class StudentController extends HttpServlet {
    //请使用以下JSON测试增加功能（id为空）
    //{"description":"id为null新学生","no":"0201","remarks":"","grade":{"description":"管理工程","id":2,"no":"02","remarks":"最好的学院"}}
    //请使用以下JSON测试修改功能
    //{"description":"修改id=1的学生","id":1,"no":"0201","remarks":"","grade":{"description":"管理工程","id":2,"no":"02","remarks":"最好的学院"}}

    /**
     * POST, http://49.235.240.61:8080/Flipped/student.ctl, 增加学生
     * 增加一个学生对象：将来自前端请求的JSON对象，增加到数据库表中
     *
     * @param request  请求对象
     * @param response 响应对象
     * @throws ServletException
     * @throws IOException
     */
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        //根据request对象，获得代表参数的JSON字串
        String student_json = JSONUtil.getJSON(request);

        //将JSON字串解析为Student对象
        Student studentToAdd = JSON.parseObject(student_json, Student.class);
        //创建JSON对象message，以便往前端响应信息
        JSONObject message = new JSONObject();
        //在数据库表中增加Student对象
        boolean added = StudentService.getInstance().add(studentToAdd);
        if (added) {
            message.put("message", "增加成功!");
        } else {
            message.put("message", "增加失败!");
        }
        //响应message到前端
        response.getWriter().println(message);
    }

    /**
     * DELETE, http://49.235.240.61:8080/Flipped/student.ctl?id=1, 删除id=1的学生
     * 删除一个学生对象：根据来自前端请求的id，删除数据库表中id的对应记录
     *
     * @param request
     * @param response
     * @throws ServletException
     * @throws IOException
     */
    @Override
    protected void doDelete(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        //读取参数id
        String id_str = request.getParameter("id");
        int id = Integer.parseInt(id_str);
        //创建JSON对象message，以便往前端响应信息
        JSONObject message = new JSONObject();
        //到数据库表中删除对应的学生

        boolean deleted = StudentService.getInstance().delete(id);
        if (deleted) {
            message.put("message", "删除成功!");
        } else {
            message.put("message", "删除失败!");
        }
        //响应message到前端
        response.getWriter().println(message);
    }

    /**
     * PUT, http://49.235.240.61:8080/Flipped/student.ctl, 修改学生
     * <p>
     * 修改一个学生对象：将来自前端请求的JSON对象，更新数据库表中相同id的记录
     *
     * @param request
     * @param response
     * @throws ServletException
     * @throws IOException
     */
    @Override
    protected void doPut(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        String student_json = JSONUtil.getJSON(request);
        //将JSON字串解析为Student对象
        Student studentToAdd = JSON.parseObject(student_json, Student.class);
        //创建JSON对象message，以便往前端响应信息
        JSONObject message = new JSONObject();
        //到数据库表修改Student对象对应的记录
        try {
            StudentService.getInstance().update(studentToAdd);
            message.put("message", "修改成功!");
        } catch (SQLException e) {
            message.put("message", "数据库操作异常!");
            e.printStackTrace();
        } catch (Exception e) {
            message.put("message", "网络异常!");
            e.printStackTrace();
        }
        //响应message到前端
        response.getWriter().println(message);
    }

    /**
     * GET, http://49.235.240.61:8080/Flipped/student.ctl?id=1, 查询id=1的学生
     * GET, http://49.235.240.61:8080/Flipped/student.ctl, 查询所有的学生
     * 把一个或所有学生对象响应到前端
     *
     * @param request
     * @param response
     * @throws ServletException
     * @throws IOException
     */
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        //读取参数id
        String id_str = request.getParameter("id");
        String no_str = request.getParameter("no");
        String grade_id_str = request.getParameter("grade_id");

        //创建JSON对象message，以便往前端响应信息
        JSONObject message = new JSONObject();
        try {
            //如果id = null, 表示响应所有学生对象，否则响应id指定的学生对象
            if (id_str == null && grade_id_str == null && no_str == null) {
                responseStudents(response);
            } else if (grade_id_str != null) {
                int grade_id = Integer.parseInt(grade_id_str);
                responseStudentByGrade(grade_id, response);
            } else if (no_str != null) {
                responseStudentByNo(no_str, response);
            } else {
                int id = Integer.parseInt(id_str);
                responseStudent(id, response);
            }
        } catch (SQLException e) {
            message.put("message", "数据库操作异常!");
            e.printStackTrace();
            //响应message到前端
            response.getWriter().println(message);
        } catch (Exception e) {
            message.put("message", "网络异常!");
            e.printStackTrace();
            //响应message到前端
            response.getWriter().println(message);
        }
    }

    //响应一个学生对象
    private void responseStudent(int id, HttpServletResponse response)
            throws ServletException, IOException, SQLException {
        //根据id查找学生
        Student student = StudentService.getInstance().find(id);
        String student_json = JSON.toJSONString(student);

        //响应student_json到前端
        response.getWriter().println(student_json);
    }

    //响应所有学生对象
    private void responseStudents(HttpServletResponse response)
            throws ServletException, IOException, SQLException {
        //获得所有学生
        Collection<Student> students = StudentService.getInstance().findAll();
        String students_json = JSON.toJSONString(students, SerializerFeature.DisableCircularReferenceDetect);

        //响应students_json到前端
        response.getWriter().println(students_json);
    }

    private void responseStudentByGrade(int grade_id, HttpServletResponse response)
            throws ServletException, IOException, SQLException {
        //获得对应的所有学生
        Collection<Student> students = StudentService.getInstance().findAllByGrade(grade_id);
        String students_json = JSON.toJSONString(students, SerializerFeature.DisableCircularReferenceDetect);

        //响应students_json到前端
        response.getWriter().println(students_json);
    }

    private void responseStudentByNo(String no, HttpServletResponse response)
            throws ServletException, IOException, SQLException {
        //获得对应的所有学生
        Collection<Student> students = StudentService.getInstance().findAllByNo(no);
        String students_json = JSON.toJSONString(students, SerializerFeature.DisableCircularReferenceDetect);

        //响应students_json到前端
        response.getWriter().println(students_json);
    }
}
