-- Etapa 5: índices de apoio às consultas analíticas dos relatórios.
-- Todos os relatórios agregam via projection/JPQL direto no banco (nunca
-- carregando entidade para somar em Java) — ver RelatorioService.

CREATE INDEX idx_beneficiario_ativo_status ON beneficiario (ativo, status);
CREATE INDEX idx_beneficiario_endereco_bairro ON beneficiario_endereco (bairro);
CREATE INDEX idx_beneficiario_endereco_cidade ON beneficiario_endereco (cidade);
CREATE INDEX idx_retirada_tipo_retirante ON retirada_cesta (tipo_retirante);
CREATE INDEX idx_retirada_mes_cancelada ON retirada_cesta (mes_referencia, cancelada);
CREATE INDEX idx_retirada_confirmacao_confirmado_em ON retirada_confirmacao (confirmado_em);
