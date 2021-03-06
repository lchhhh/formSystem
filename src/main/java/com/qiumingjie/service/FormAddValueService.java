package com.qiumingjie.service;

import com.qiumingjie.dao.dict.FormDictRepository;
import com.qiumingjie.dao.dict.ItemDictRepository;
import com.qiumingjie.dao.dict.RelationRepository;
import com.qiumingjie.dao.table.FormMainRepository;
import com.qiumingjie.dao.table.FormValueRepository;
import com.qiumingjie.dto.FormDataDto;
import com.qiumingjie.entities.evaluate.dict.FormDict;
import com.qiumingjie.entities.evaluate.dict.Relation;
import com.qiumingjie.entities.evaluate.dict.RelationKeys;
import com.qiumingjie.entities.evaluate.table.FormMain;
import com.qiumingjie.handler.JsonHandler;
import com.qiumingjie.utils.CommonUtils;
import com.qiumingjie.utils.CopyUtils;
import com.qiumingjie.utils.FormUtil;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * @author QiuMingJie
 * @date 2020-01-27 0:19
 * @description
 */
@Service
public class FormAddValueService {

    @Resource
    private FormDictRepository formDictRepository;

    @Resource
    private FormMainRepository formMainRepository;

    @Resource
    private FormValueRepository formValueRepository;

    @Resource
    private ItemDictRepository itemDictRepository;

    @Resource
    private RelationRepository relationRepository;

    @Resource
    private FormDictService formDictService;

    @Transactional(rollbackFor = RuntimeException.class)
    public JsonHandler saveOrUpdateForm(FormDataDto formDataDto) {
        if (CommonUtils.empty(formDataDto.getFormId())) {
            //不存在formId则是新建操作！
            if (CommonUtils.empty(formDataDto.getTemplateFormId())) {
                return JsonHandler.fail("新建表单失败：获取模板表单id失败");
            }
            Optional<FormDict> formDict=formDictService.existFormDict(formDataDto.getTemplateFormId());
            if (!formDict.isPresent()) {
                return JsonHandler.fail("新建表单失败：表单模板不存在");
            }
            //暂时不知道怎么获取创建者
//            formDataDto.setCreator("？？");
            FormMain formMainByFormIdOrderByFormIdDesc =formMainRepository.findFormMainBytemplateIdLike(formDataDto.getFormId()).get(0);
            if (CommonUtils.empty(formMainByFormIdOrderByFormIdDesc) || formMainByFormIdOrderByFormIdDesc.getFormId() == null) {
                formDataDto.setFormId(FormUtil.caculFormEntityId(formDataDto.getTemplateFormId()));
            }else {
                formDataDto.setFormId(FormUtil.caculFormEntityId(formMainByFormIdOrderByFormIdDesc.getFormId()));
            }
        }
        formDataDto.initFormValueFormId();
        FormMain formMain = CopyUtils.transfer(formDataDto, new FormMain());
        formMainRepository.save(formMain);
        formValueRepository.saveAll(formDataDto.getFormValues());
        return JsonHandler.succeed(formDataDto);
    }


    public JsonHandler getForm(String formId) {
        Optional<FormMain> formMain = formMainRepository.findById(formId);
        FormDataDto formDataDto;
        if (formMain.isPresent()) {
            formDataDto = CopyUtils.transfer( formMain.get(),new FormDataDto());
        }else {
            return JsonHandler.fail("表单不存在");
        }
        Optional<FormDict> formDict = formDictRepository.findById(FormUtil.getFormDictId(formId));
        if (formDict.isPresent()) {
            formDataDto.setFormName(formDict.get().getFormName());
            formDataDto.setType(formDict.get().getFormType());
        }else {
            return JsonHandler.fail("获取模板表失败");
        }
        List<Relation> relationList = relationRepository.findAllById_FormDictId(formDict.get().getFormDictId());
        List<String> itemIdList=new ArrayList<>();
//        relationList.forEach(x -> itemIdList.add(x.getId().getItemId()));
//        List<ItemDict> itemDictList = itemDictRepository.findAllById(itemIdList);
        List<RelationKeys> relationKeys = new ArrayList<>();
        relationList.forEach(x -> relationKeys.add(new RelationKeys(formId,x.getId().getItemId())));
        formDataDto.setFormValues(formValueRepository.findAllById(relationKeys));
        return JsonHandler.succeed(formDataDto);
    }

    public JsonHandler deleteForm(String formId) {
        formMainRepository.deleteById(formId);
        List<Relation> relationList = relationRepository.findAllById_FormDictId(FormUtil.getFormDictId(formId));
        List<RelationKeys> relationKeys = new ArrayList<>();
        relationList.forEach(x -> relationKeys.add(new RelationKeys(formId, x.getId().getItemId())));
        relationRepository.deleteByIdList(relationKeys);
        return JsonHandler.succeed();
    }
}
