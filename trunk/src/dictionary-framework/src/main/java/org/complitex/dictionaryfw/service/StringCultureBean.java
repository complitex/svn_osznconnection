/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.complitex.dictionaryfw.service;

import com.google.common.base.Predicate;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import org.apache.wicket.util.string.Strings;
import org.complitex.dictionaryfw.entity.InsertParameter;
import org.complitex.dictionaryfw.entity.StringCulture;
import org.complitex.dictionaryfw.mybatis.Transactional;

import javax.ejb.EJB;
import javax.ejb.Stateless;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.NoSuchElementException;

/**
 *
 * @author Artem
 */
@Stateless(name = "StringCultureBean")
public class StringCultureBean extends AbstractBean {

    private static final String STRING_CULTURE_NAMESPACE = "org.complitex.dictionaryfw.entity.StringCulture";

    @EJB(beanName = "SequenceBean")
    private SequenceBean sequenceBean;

    @EJB(beanName = "LocaleBean")
    private LocaleBean localeBean;

    @Transactional
    public Long insertStrings(List<StringCulture> strings, String entityTable) {
        if (strings != null && !strings.isEmpty()) {
            boolean allValuesAreEmpty = true;
            for (StringCulture string : strings) {
                if (!Strings.isEmpty(string.getValue())) {
                    allValuesAreEmpty = false;
                    break;
                }
            }
            if (allValuesAreEmpty) {
                return null;
            }

            long stringId = sequenceBean.nextStringId(entityTable);
            for (StringCulture string : strings) {
                if (!Strings.isEmpty(string.getValue())) {
                    string.setId(stringId);
                    insert(string, entityTable);
                }
            }
            return stringId;
        }
        return null;
    }

    @Transactional
    public void insert(StringCulture stringCulture, String entityTable) {
        if (Strings.isEmpty(entityTable)) {
            sqlSession().insert(STRING_CULTURE_NAMESPACE + ".insertDescriptionData", stringCulture);
        } else {
            sqlSession().insert(STRING_CULTURE_NAMESPACE + ".insert", new InsertParameter(entityTable, stringCulture));
        }
    }

    public List<StringCulture> newStringCultures() {
        List<StringCulture> strings = Lists.newArrayList();
        updateForNewLocales(strings);
        return strings;
    }

    public void updateForNewLocales(List<StringCulture> stringCultures) {
        for (final String locale : localeBean.getAllLocales()) {
            try {
                Iterables.find(stringCultures, new Predicate<StringCulture>() {

                    @Override
                    public boolean apply(StringCulture string) {
                        return locale.equals(string.getLocale());
                    }
                });
            } catch (NoSuchElementException e) {
                stringCultures.add(new StringCulture(locale, null));
            }
        }
    }

    public StringCulture getSystemStringCulture(List<StringCulture> stringCultures) {
        return Iterables.find(stringCultures, new Predicate<StringCulture>() {

            @Override
            public boolean apply(StringCulture stringCulture) {
                return stringCulture.getLocale().equals(localeBean.getSystemLocale());
            }
        });
    }

    public String displayValue(List<StringCulture> strings, final Locale locale) {
        String value = null;
        try {
            value = Iterables.find(strings, new Predicate<StringCulture>() {

                @Override
                public boolean apply(StringCulture string) {
                    return locale.getLanguage().equalsIgnoreCase(string.getLocale());

                }
            }).getValue();

        } catch (NoSuchElementException e) {
        }
        if (Strings.isEmpty(value)) {
            try {
                value = Iterables.find(strings, new Predicate<StringCulture>() {

                    @Override
                    public boolean apply(StringCulture string) {
                        return localeBean.getSystemLocale().equalsIgnoreCase(string.getLocale());
                    }
                }).getValue();
            } catch (NoSuchElementException e) {
            }
        }
        return value;
    }

    public List<StringCulture> findStrings(long id, String entityTable) {
        Map<String, Object> params = ImmutableMap.<String, Object>builder().
                put("table", entityTable).
                put("id", id).
                build();
        return sqlSession().selectList(STRING_CULTURE_NAMESPACE + ".find", params);
    }
}
