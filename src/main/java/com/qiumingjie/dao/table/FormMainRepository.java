package com.qiumingjie.dao.table;

import com.qiumingjie.entities.evaluate.table.FormMain;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * @author QiuMingJie
 * @date 2020-01-22 12:11
 * @description
 */
@Repository
public interface FormMainRepository extends JpaRepository<FormMain, String> {

//    FormMain findFormMainByFormIdOrderByFormIdDesc(String formId);

    @Query("select a from FormMain a where a.formId like ?1%  order by  a.formId desc ")
    List< FormMain> findFormMainBytemplateIdLike(String templateId);


}
