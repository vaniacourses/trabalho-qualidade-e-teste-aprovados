package br.winxbank.sistemabancario;
import java.util.Scanner;

/**
 * @author Dani
 * Esta classe é responsável por representar uma entidade ContaCorrente.
 */
public class ContaCorrente extends Conta implements OperacoesAutomaticas{

    private static final int OPCAO_DEBITO = 1;
    private static final int OPCAO_CREDITO = 2;
    private static final int CONFIRMAR = 1;

    private CartaoCredito cartaoCredito;

    /**
     * Construtor padrão da classe conta.
     *
     * @param saldo
     * @param cartaoDebito
     * @param dividaDeEmprestimo
     */
    public ContaCorrente(int numeroConta, double saldo, Cartao cartaoDebito, double dividaDeEmprestimo, CartaoCredito cartaoCredito) {
        super(numeroConta, saldo, cartaoDebito, dividaDeEmprestimo);
        this.cartaoCredito = cartaoCredito;
    }
    

    /**
     * Método responsável por pagar fatura com o saldo da conta.
     * @param valor
     */
    public void pagarFatura(double valor){
        this.saldo-=valor;
        this.cartaoCredito.setFatura(-valor);

    }

    /**
     * Método responsável por descontar a taxa de uma conta corrente.
     */
    public void descontarTaxa(){
        this.saldo -= taxaManutencaoConta;
        movimentacaoBancaria(taxaManutencaoConta);
        Movimentacao movimentacao = new Movimentacao(taxaManutencaoConta, Movimentacao.TipoDaMovimentacao.SAIDA);
        this.setExtrato(movimentacao);

    }

    /**
     * Método da interface MovimentacaoBancaria sobrescrito responsável por movimentar dinheiro ao banco.
     * @param valor
     */
    @Override
    public void movimentacaoBancaria(double valor) {
        Banco.getInstancia().setReceitas(valor);
    }


    public CartaoCredito getCartaoCredito() {
        return cartaoCredito;
    }

    public String getTipoDaConta() {
        return "Corrente";
    }

    /**
     * Exibe os dados de um cartao e solicita confirmacao do usuario.
     * @param sc Scanner para leitura de entrada
     * @param cartao Cartao a ser exibido
     * @return true se o usuario confirmar (digitar 1)
     */
    private boolean exibirCartaoEConfirmar(Scanner sc, Cartao cartao) {
        System.out.println("------------------------------------------------");
        System.out.println(cartao.getNumero() + "\n" + cartao.getCsv());
        System.out.println("------------------------------------------------");
        System.out.println("Este e o cartao que deseja utilizar? Digite 1 (confirmar)");
        int decisao2 = sc.nextInt();
        return decisao2 == CONFIRMAR;
    }

    /**
     * Método abstrato da classe conta sobrescrito responsável por realizar uma compra.
     * @param valor
     */
    @Override
    @SuppressWarnings("resource")
    public void comprar(double valor) {
        Scanner sc = new Scanner(System.in);
        System.out.println("Você deseja pagar no debito ou no credito? 1 (debito) ou 2 (credito)");
        int decisao = sc.nextInt();
        if (decisao == OPCAO_DEBITO){
            if(exibirCartaoEConfirmar(sc, this.cartao)){
                cartao.debitar(this, valor);
                System.out.println("Valor debitado.");
            }
            else{
                System.out.println("Compra cancelada. Efetue a compra novamente.");
            }
        }
        else if(decisao == OPCAO_CREDITO){
            if(exibirCartaoEConfirmar(sc, this.cartaoCredito)){
                this.cartaoCredito.creditar(valor);
                System.out.println("Valor creditado.");
            }
            else{
                System.out.println("Compra cancelada. Efetue a compra novamente.");
            }
        }
    }
}
