package com.max.dao;

import com.max.entity.LoginTicket;
import org.apache.ibatis.annotations.*;

/**
 * 写完后，新创建测试用例，验证sql语句是否正确
 * 然后到service里面继续写
 */

@Mapper
public interface LoginTicketMapper {

    @Insert({
            "insert into login_ticket (user_id,ticket,status,expired) ",
            "values(#{userId},#{ticket},#{status},#{expired})"
    })
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int InsertLoginTicket(LoginTicket loginTicket);  //返回增加的数量

    //查数据，返回的是LoginTicket
    @Select({
            "select id,user_id,ticket,status,expired ",
            "from login_ticket where ticket = #{ticket}"
    })
    LoginTicket SelectLoginTicket(String ticket);

    //改变数据的状态
    @Update({
            "<script>",
            "update login_ticket set status = #{status} where ticket = #{ticket}",
            "<if test = \"ticket != null\">",
            "and 1=1",
            "</if>",
            "</script>"
    })
    int UpdateLoginStatus(String ticket, int status);


}
