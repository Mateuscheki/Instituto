/**
 * JS mínimo do módulo Benefícios: máscaras de CPF/telefone/CEP nos campos
 * marcados com as classes .mascara-cpf/.mascara-telefone/.mascara-cep (ver
 * templates/fragments/campos.html), e um helper genérico para
 * habilitar/desabilitar campos dependentes (ex.: "tem filhos?" -> libera
 * "quantidade de filhos"). Nenhuma regra de negócio aqui — só formatação de
 * digitação e toggle de UI.
 */
(function () {
    "use strict";

    function apenasDigitos(valor) {
        return (valor || "").replace(/\D/g, "");
    }

    function mascararCpf(valor) {
        const digitos = apenasDigitos(valor).slice(0, 11);
        return digitos
            .replace(/(\d{3})(\d)/, "$1.$2")
            .replace(/(\d{3})(\d)/, "$1.$2")
            .replace(/(\d{3})(\d{1,2})$/, "$1-$2");
    }

    function mascararTelefone(valor) {
        const digitos = apenasDigitos(valor).slice(0, 11);
        if (digitos.length <= 10) {
            return digitos
                .replace(/(\d{2})(\d)/, "($1) $2")
                .replace(/(\d{4})(\d{1,4})$/, "$1-$2");
        }
        return digitos
            .replace(/(\d{2})(\d)/, "($1) $2")
            .replace(/(\d{5})(\d{1,4})$/, "$1-$2");
    }

    function mascararCep(valor) {
        const digitos = apenasDigitos(valor).slice(0, 8);
        return digitos.replace(/(\d{5})(\d{1,3})$/, "$1-$2");
    }

    function aplicarMascara(seletor, funcaoMascara) {
        document.querySelectorAll(seletor).forEach(function (campo) {
            campo.addEventListener("input", function () {
                const posicaoOriginal = campo.selectionStart;
                const tamanhoAntes = campo.value.length;
                campo.value = funcaoMascara(campo.value);
                const diferenca = campo.value.length - tamanhoAntes;
                if (posicaoOriginal !== null) {
                    campo.setSelectionRange(posicaoOriginal + diferenca, posicaoOriginal + diferenca);
                }
            });
        });
    }

    /**
     * Habilita/desabilita e limpa um campo dependente conforme a resposta de
     * um campo "gatilho" (ex.: select SIM/NAO/NAO_INFORMADO). Uso típico:
     *   beneficiosToggleDependente('#temFilhos', '#quantidadeFilhos', ['SIM']);
     */
    window.beneficiosToggleDependente = function (seletorGatilho, seletorDependente, valoresQueHabilitam) {
        const gatilho = document.querySelector(seletorGatilho);
        const dependente = document.querySelector(seletorDependente);
        if (!gatilho || !dependente) {
            return;
        }

        function atualizar() {
            const habilitado = valoresQueHabilitam.includes(gatilho.value);
            dependente.disabled = !habilitado;
            dependente.closest(".mb-3, .form-group")?.classList.toggle("d-none", !habilitado);
            if (!habilitado) {
                dependente.value = "";
            }
        }

        gatilho.addEventListener("change", atualizar);
        atualizar();
    };

    /**
     * Liga a busca automática de endereço por CEP (ViaCEP) a um campo de CEP.
     * Se a consulta falhar (rede, CEP inexistente, timeout), não faz nada —
     * o operador preenche manualmente, a tela nunca trava.
     * Uso: beneficiosLigarBuscaCep('#cep', {rua:'#rua', bairro:'#bairro', cidade:'#cidade', uf:'#uf'});
     */
    window.beneficiosLigarBuscaCep = function (seletorCep, seletoresDestino) {
        const campoCep = document.querySelector(seletorCep);
        if (!campoCep) {
            return;
        }

        campoCep.addEventListener("blur", function () {
            const cep = apenasDigitos(campoCep.value);
            if (cep.length !== 8) {
                return;
            }

            const controlador = new AbortController();
            const timeout = setTimeout(() => controlador.abort(), 5000);

            fetch("https://viacep.com.br/ws/" + cep + "/json/", { signal: controlador.signal })
                .then(function (resposta) {
                    if (!resposta.ok) {
                        throw new Error("Falha ao consultar CEP");
                    }
                    return resposta.json();
                })
                .then(function (dados) {
                    if (dados.erro) {
                        return;
                    }
                    preencherSeVazio(seletoresDestino.rua, dados.logradouro);
                    preencherSeVazio(seletoresDestino.bairro, dados.bairro);
                    preencherSeVazio(seletoresDestino.cidade, dados.localidade);
                    preencherSeVazio(seletoresDestino.uf, dados.uf);
                })
                .catch(function () {
                    // Silencioso de propósito: consulta de CEP nunca bloqueia o atendimento.
                })
                .finally(function () {
                    clearTimeout(timeout);
                });
        });
    };

    function preencherSeVazio(seletor, valor) {
        if (!seletor || !valor) {
            return;
        }
        const campo = document.querySelector(seletor);
        if (campo && !campo.value) {
            campo.value = valor;
        }
    }

    document.addEventListener("DOMContentLoaded", function () {
        aplicarMascara(".mascara-cpf", mascararCpf);
        aplicarMascara(".mascara-telefone", mascararTelefone);
        aplicarMascara(".mascara-cep", mascararCep);
    });
})();
