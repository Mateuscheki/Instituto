package br.com.imgazin.beneficios.util;

import org.springframework.beans.BeanWrapper;
import org.springframework.beans.BeanWrapperImpl;

import java.beans.PropertyDescriptor;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Reflexão genérica sobre um objeto de formulário — usado para gerar os
 * {@code <input type="hidden">} da tela de confirmação sem repetir campo a
 * campo na view nem no controller (ver
 * {@code fragments/campos.html :: hiddensDoForm}). Funciona para qualquer
 * Form do módulo (não só {@code BeneficiarioForm}), pensado para reuso nos
 * próximos fluxos de confirmação (retirada, etc.).
 */
public final class FormUtils {

    private FormUtils() {
    }

    public static Map<String, Object> paraCamposOcultos(Object form) {
        Map<String, Object> campos = new LinkedHashMap<>();
        if (form == null) {
            return campos;
        }

        BeanWrapper wrapper = new BeanWrapperImpl(form);
        for (PropertyDescriptor descritor : wrapper.getPropertyDescriptors()) {
            String nome = descritor.getName();
            if ("class".equals(nome) || descritor.getWriteMethod() == null) {
                continue;
            }
            Object valor = wrapper.getPropertyValue(nome);
            campos.put(nome, valor == null ? "" : valor.toString());
        }
        return campos;
    }
}
