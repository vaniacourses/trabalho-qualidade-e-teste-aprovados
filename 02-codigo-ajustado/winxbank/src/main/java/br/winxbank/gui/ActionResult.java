package br.winxbank.gui;

import br.winxbank.sistemaclientes.Cliente;

/**
 * Resultado de uma operação disparada pela GUI.
 *
 * <p>Carrega a mensagem de feedback que será exibida no painel de saída, o
 * status (sucesso/erro) e, quando a operação altera o cliente logado (login,
 * abertura de conta com upgrade para ClienteWinx, logout), a nova referência do
 * cliente da sessão.</p>
 */
public final class ActionResult {

    private final boolean ok;
    private final String message;
    private final Cliente updatedCliente;
    private final boolean sessionChanged;

    private ActionResult(boolean ok, String message, Cliente updatedCliente, boolean sessionChanged) {
        this.ok = ok;
        this.message = message;
        this.updatedCliente = updatedCliente;
        this.sessionChanged = sessionChanged;
    }

    /** Operação concluída com sucesso, sem alterar o cliente da sessão. */
    public static ActionResult ok(String message) {
        return new ActionResult(true, message, null, false);
    }

    /** Operação concluída com sucesso e que substitui o cliente logado. */
    public static ActionResult okWithSession(String message, Cliente cliente) {
        return new ActionResult(true, message, cliente, true);
    }

    /** Operação que falhou (regra de negócio ou validação). */
    public static ActionResult error(String message) {
        return new ActionResult(false, message, null, false);
    }

    public boolean isOk() {
        return ok;
    }

    public String getMessage() {
        return message;
    }

    public Cliente getUpdatedCliente() {
        return updatedCliente;
    }

    /** Indica que o cliente da sessão deve ser substituído por {@link #getUpdatedCliente()}. */
    public boolean isSessionChanged() {
        return sessionChanged;
    }
}
