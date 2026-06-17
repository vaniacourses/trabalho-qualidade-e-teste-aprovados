package br.winxbank.sistemabancario;


import br.winxbank.exception.BankAccountNotFoundException;
import br.winxbank.random.RandomNumberGenerator;
import br.winxbank.sistemaclientes.Cliente;
import br.winxbank.sistemaclientes.RegistroDeClientes;

import java.io.Serializable;
import java.text.DecimalFormat;
import java.util.Random;
import java.util.Scanner;

/**
 * @author Natália.
 * Classe responsável por administrar um banco.
 */
public class Banco implements Serializable {

    public double receitas;
    public double despesas;
    private static Banco instancia;
    private final transient Random randomNum = new Random();
    
    public Conta abrirNovaConta() {
        return abrirNovaConta(new Scanner(System.in));
    }

    /**
     * Método responsável por abrir uma nova conta.
     */
    public Conta abrirNovaConta() {
        return abrirNovaConta(new Scanner(System.in));
    }

    /**
     * Abre uma nova conta usando o scanner informado. Isso evita múltiplos
     * scanners concorrendo pelo mesmo System.in durante fluxos de cadastro.
     */
    public Conta abrirNovaConta(Scanner sc) {
        int decisao = 0;
        System.out.println("Qual tipo de conta conta deseja abrir? Digite 1 (Corrente) ou 2 (Poupanca)");
        int decisao = Integer.parseInt(sc.nextLine().trim());
        if (decisao == 1) {
            System.out.println("Voce esta criando uma conta corrente...");
            int numeroCartao = RandomNumberGenerator.gerarNumCartao();
            int csv = RandomNumberGenerator.gerarCsv();
            Cartao cartao = new Cartao(numeroCartao, csv);
            CartaoCredito cartaoCredito = new CartaoCredito(numeroCartao, csv);
            int numeroConta = RandomNumberGenerator.gerarNumConta();
            System.out.println("Digite o saldo que deseja colocar na sua conta ");
            double saldo = Double.parseDouble(sc.nextLine().trim());
            Conta contaCorrente = new ContaCorrente(numeroConta, saldo, cartao, 0, cartaoCredito);
            System.out.println("Sua conta corrente foi criada com sucesso!");
            return contaCorrente;
        } 
        else if (decisao == 2) {
            System.out.println("Voce esta criando uma conta poupanca...");
            int numeroCartao = RandomNumberGenerator.gerarNumCartao();
            int csv = RandomNumberGenerator.gerarCsv();
            Cartao cartao = new Cartao(numeroCartao, csv);
            int numeroConta = RandomNumberGenerator.gerarNumConta();
            System.out.println("Digite o saldo que deseja colocar na sua conta:");
            double saldo = Double.parseDouble(sc.nextLine().trim());
            ContaPoupanca contaPoupanca = new ContaPoupanca(numeroConta, saldo, cartao, 0);
            System.out.println("Sua conta poupanca foi criada com sucesso!");
            return contaPoupanca;
        }
    return null;
    }

    /**
     * Método responsável por encerrar uma conta de um cliente.
     * @param cliente
     */
    public void fecharConta(Cliente cliente){
        System.out.println("Digite o numero da conta que deseja apagar:");
        Scanner sc = new Scanner(System.in);
        int numeroConta = sc.nextInt();
        Conta contaSelecionada = cliente.selecionarConta(numeroConta);
        if(contaSelecionada == null){
            throw new BankAccountNotFoundException();
        }
        cliente.apagarConta(contaSelecionada);
        System.out.println("Sua conta foi apagada com sucesso!");
    }

    /**
     * Método responsável por realizar as movimentações entre banco e conta.
     * Tais quais: acrescentar rendimento em uma poupança e cobrar juros da fatura do cartao de credito e descontar taxa de uma conta corrente.
     */
    public void movimentarEntreBancoConta(){
        for(Cliente cliente : RegistroDeClientes.getInstancia().getClientes()){
            for(Conta conta : cliente.getContas()){
                processarMovimentacaoDaConta(conta);
            }
        }
    }

    /**
     * Processa as movimentações automáticas de uma única conta:
     * cobrança de juros do empréstimo e, conforme o tipo da conta,
     * rendimento (poupança) ou desconto de taxa e juros do cartão (corrente).
     * @param conta conta a ser processada
     */
    private void processarMovimentacaoDaConta(Conta conta){
        conta.cobrarJurosEmprestimo();
        if(conta.getClass() == ContaPoupanca.class){
            ((ContaPoupanca) conta).acrescentarRendimento();
        }
        else if(conta.getClass() == ContaCorrente.class){
            ContaCorrente contaCorrente = (ContaCorrente) conta;
            contaCorrente.descontarTaxa();
            if(contaCorrente.getCartaoCredito().getFatura() > 0){
                contaCorrente.getCartaoCredito().cobrarJuros();
            }
        }
    }

    /**
     * Método responsável por visualizar detalhes do banco.
     */
    public void printarBanco(){
        System.out.println("Despesas do banco: " + new DecimalFormat("0.00").format(this.despesas));
        System.out.println("Receitas do banco: " + new DecimalFormat("0.00").format(this.receitas));
        if(this.despesas > this.receitas){
            this.despesas/=randomNum.nextInt(1000000000, 1999999999);
        }
    }


    public double getDespesas() {
        return despesas;
    }

    public double getReceitas() {
        return receitas;
    }

    public void setReceitas(double valor) {
        if(valor >= 0){
            this.receitas += valor;
        }
    }

    public void setDespesas(double valor) {
        if(valor >= 0){
            this.despesas += valor;
        }
    }

    public void setBanco(Banco banco){
        this.despesas = banco.getDespesas();
        this.receitas = banco.getReceitas();
    }

    /**
     * Singleton que só permite uma instância do objeto ser criada, quando o atributo estático instancia tem o valor nulo.
     * @return
     */
    public static Banco getInstancia() {
        if (instancia == null) {
            instancia = new Banco();
        }
        return instancia;
    }

}
