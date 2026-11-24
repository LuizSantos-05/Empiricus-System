import 'package:flutter/material.dart';

enum MesaStatus { disponivel, ocupada, reservada }

class Mesa {
  final int numero;
  final int capacidade;
  MesaStatus status;
  String? clienteAtual;

  Mesa({
    required this.numero,
    required this.capacidade,
    this.status = MesaStatus.disponivel,
    this.clienteAtual,
  });
}

class ClienteFila {
  final int id;
  final String nome;
  final String telefone;
  final int pessoas;
  final String email;
  final String observacoes;
  bool chamado;

  ClienteFila({
    required this.id,
    required this.nome,
    required this.telefone,
    required this.pessoas,
    required this.email,
    required this.observacoes,
    this.chamado = false,
  });
}

class HomeScreen extends StatefulWidget {
  const HomeScreen({super.key});

  @override
  State<HomeScreen> createState() => _HomeScreenState();
}

class _HomeScreenState extends State<HomeScreen> {
  final List<Mesa> _mesas = [
    Mesa(numero: 1, capacidade: 4, status: MesaStatus.disponivel),
    Mesa(
      numero: 2,
      capacidade: 2,
      status: MesaStatus.ocupada,
      clienteAtual: 'Lucas',
    ),
    Mesa(numero: 3, capacidade: 4, status: MesaStatus.disponivel),
    Mesa(
      numero: 4,
      capacidade: 6,
      status: MesaStatus.reservada,
      clienteAtual: 'Reserva 20h',
    ),
    Mesa(
      numero: 5,
      capacidade: 2,
      status: MesaStatus.ocupada,
      clienteAtual: 'Ana',
    ),
    Mesa(numero: 6, capacidade: 4, status: MesaStatus.disponivel),
    Mesa(
      numero: 7,
      capacidade: 4,
      status: MesaStatus.ocupada,
      clienteAtual: 'Carlos',
    ),
    Mesa(numero: 8, capacidade: 6, status: MesaStatus.disponivel),
    Mesa(
      numero: 9,
      capacidade: 2,
      status: MesaStatus.reservada,
      clienteAtual: 'Reserva 21h',
    ),
    Mesa(numero: 10, capacidade: 4, status: MesaStatus.disponivel),
  ];

  final List<ClienteFila> _fila = [
    ClienteFila(
      id: 47,
      nome: 'Takeshi Yamamoto',
      telefone: '(45) 99888-1111',
      pessoas: 4,
      email: 'takeshi@email.com',
      observacoes: '',
    ),
    ClienteFila(
      id: 48,
      nome: 'Maria Santos',
      telefone: '(45) 97777-2222',
      pessoas: 2,
      email: 'maria@email.com',
      observacoes: '',
    ),
  ];

  final _nomeController = TextEditingController();
  final _telController = TextEditingController();
  final _pessController = TextEditingController(text: '1');
  final _emailController = TextEditingController();
  final _obsController = TextEditingController();

  MesaStatus? _filtroStatus; // null = mostra todas

  int _contarPorStatus(MesaStatus status) =>
      _mesas.where((m) => m.status == status).length;

  void _alternarStatusMesa(Mesa mesa) {
    setState(() {
      if (mesa.status == MesaStatus.disponivel) {
        mesa.status = MesaStatus.ocupada;
      } else if (mesa.status == MesaStatus.ocupada) {
        mesa.status = MesaStatus.reservada;
        mesa.clienteAtual = null;
      } else {
        mesa.status = MesaStatus.disponivel;
        mesa.clienteAtual = null;
      }
    });
  }

  void _atenderCliente(ClienteFila cliente) {
    final indexDisponivel = _mesas.indexWhere(
      (m) =>
          m.status == MesaStatus.disponivel && m.capacidade >= cliente.pessoas,
    );

    // Se não houver mesa com capacidade exata/maior, pega qualquer disponível
    final fallbackIndex = indexDisponivel != -1
        ? indexDisponivel
        : _mesas.indexWhere((m) => m.status == MesaStatus.disponivel);

    if (fallbackIndex == -1) {
      ScaffoldMessenger.of(context).showSnackBar(
        const SnackBar(
          backgroundColor: Colors.white,
          content: Text(
            'Nenhuma mesa disponível no momento.',
            style: TextStyle(color: Colors.black),
          ),
        ),
      );
      return;
    }

    setState(() {
      _mesas[fallbackIndex].status = MesaStatus.ocupada;
      _mesas[fallbackIndex].clienteAtual = cliente.nome;
      _fila.removeWhere((c) => c.id == cliente.id);
    });

    ScaffoldMessenger.of(context).showSnackBar(
      SnackBar(
        backgroundColor: Colors.white,
        content: Text(
          '${cliente.nome} alocado na Mesa ${_mesas[fallbackIndex].numero}.',
          style: const TextStyle(color: Colors.black),
        ),
      ),
    );
  }

  void _adicionarNaFila() {
    final nome = _nomeController.text.trim();
    final pessoas = int.tryParse(_pessController.text.trim()) ?? 0;

    if (nome.isEmpty || pessoas < 1) {
      ScaffoldMessenger.of(context).showSnackBar(
        const SnackBar(
          backgroundColor: Colors.white,
          content: Text(
            'Preencha Nome e quantidade de Pessoas (mín. 1).',
            style: TextStyle(color: Colors.black),
          ),
        ),
      );
      return;
    }

    final randomId = (DateTime.now().millisecondsSinceEpoch % 90) + 10;

    setState(() {
      _fila.add(
        ClienteFila(
          id: randomId,
          nome: nome,
          telefone: _telController.text.trim().isEmpty
              ? '(45) 00000-0000'
              : _telController.text.trim(),
          pessoas: pessoas,
          email: _emailController.text.trim().isEmpty
              ? 'N/I'
              : _emailController.text.trim(),
          observacoes: _obsController.text.trim(),
        ),
      );
    });

    _nomeController.clear();
    _telController.clear();
    _pessController.text = '1';
    _emailController.clear();
    _obsController.clear();
  }

  Color _corBorda(MesaStatus status) {
    switch (status) {
      case MesaStatus.disponivel:
        return const Color(0xFF4ADE80); // Verde (livre)
      case MesaStatus.ocupada:
        return const Color(0xFFF87171); // Vermelha (ocupada)
      case MesaStatus.reservada:
        return const Color(0xFF60A5FA); // Azul (reservada)
    }
  }

  Color _corFundo(MesaStatus status) {
    switch (status) {
      case MesaStatus.disponivel:
        return const Color(0xFF132A1C);
      case MesaStatus.ocupada:
        return const Color(0xFF2B1414);
      case MesaStatus.reservada:
        return const Color(0xFF132238);
    }
  }

  String _labelStatus(MesaStatus status) {
    switch (status) {
      case MesaStatus.disponivel:
        return 'DISPONÍVEL';
      case MesaStatus.ocupada:
        return 'OCUPADA';
      case MesaStatus.reservada:
        return 'RESERVADA';
    }
  }

  @override
  void dispose() {
    _nomeController.dispose();
    _telController.dispose();
    _pessController.dispose();
    _emailController.dispose();
    _obsController.dispose();
    super.dispose();
  }

  @override
  Widget build(BuildContext context) {
    final dispCount = _contarPorStatus(MesaStatus.disponivel);
    final ocupCount = _contarPorStatus(MesaStatus.ocupada);
    final resvCount = _contarPorStatus(MesaStatus.reservada);

    final mesasVisiveis = _filtroStatus == null
        ? _mesas
        : _mesas.where((m) => m.status == _filtroStatus).toList();

    return Scaffold(
      backgroundColor: const Color(0xFF121212),
      appBar: AppBar(
        backgroundColor: const Color(0xFF121212),
        elevation: 0,
        bottom: PreferredSize(
          preferredSize: const Size.fromHeight(1),
          child: Container(color: Colors.white12, height: 1),
        ),
        title: Row(
          children: [
            SizedBox(
              width: 34,
              height: 34,
              child: ClipOval(
                child: Image.asset(
                  'assets/images/empiricu.jpeg',
                  fit: BoxFit.cover,
                ),
              ),
            ),
            const SizedBox(width: 12),
            const Text(
              'MATSURI  |  EMPIRICUS SYSTEM',
              style: TextStyle(
                color: Colors.white,
                fontSize: 15,
                letterSpacing: 1.5,
                fontWeight: FontWeight.w400,
              ),
            ),
          ],
        ),
      ),
      body: LayoutBuilder(
        builder: (context, constraints) {
          final isWide = constraints.maxWidth >= 900;

          final mapaSection = _buildMapaCard(
            dispCount,
            ocupCount,
            resvCount,
            mesasVisiveis,
          );
          final lateralSection = Column(
            children: [
              _buildFormCard(),
              const SizedBox(height: 16),
              _buildFilaCard(),
            ],
          );

          if (isWide) {
            return SingleChildScrollView(
              padding: const EdgeInsets.all(20),
              child: Row(
                crossAxisAlignment: CrossAxisAlignment.start,
                children: [
                  Expanded(flex: 2, child: mapaSection),
                  const SizedBox(width: 20),
                  Expanded(flex: 1, child: lateralSection),
                ],
              ),
            );
          }

          return SingleChildScrollView(
            padding: const EdgeInsets.all(16),
            child: Column(
              children: [
                mapaSection,
                const SizedBox(height: 16),
                lateralSection,
              ],
            ),
          );
        },
      ),
    );
  }

  Widget _buildMapaCard(
    int dispCount,
    int ocupCount,
    int resvCount,
    List<Mesa> mesasVisiveis,
  ) {
    return Container(
      padding: const EdgeInsets.all(20),
      decoration: BoxDecoration(
        color: const Color(0xFF181818),
        borderRadius: BorderRadius.circular(8),
        border: Border.all(color: Colors.white12),
      ),
      child: Column(
        crossAxisAlignment: CrossAxisAlignment.start,
        children: [
          Row(
            mainAxisAlignment: MainAxisAlignment.spaceBetween,
            children: [
              const Text(
                'MAPA DE MESAS',
                style: TextStyle(
                  color: Colors.white,
                  fontSize: 13,
                  letterSpacing: 1.5,
                  fontWeight: FontWeight.bold,
                ),
              ),
              if (_filtroStatus != null)
                TextButton(
                  onPressed: () => setState(() => _filtroStatus = null),
                  child: const Text(
                    'MOSTRAR TODAS',
                    style: TextStyle(color: Colors.white70, fontSize: 11),
                  ),
                ),
            ],
          ),
          const SizedBox(height: 12),
          Wrap(
            spacing: 10,
            runSpacing: 8,
            children: [
              _FiltroChip(
                label: 'Disponível ($dispCount)',
                cor: const Color(0xFF4ADE80),
                active: _filtroStatus == MesaStatus.disponivel,
                onTap: () => setState(() {
                  _filtroStatus = _filtroStatus == MesaStatus.disponivel
                      ? null
                      : MesaStatus.disponivel;
                }),
              ),
              _FiltroChip(
                label: 'Ocupada ($ocupCount)',
                cor: const Color(0xFFF87171),
                active: _filtroStatus == MesaStatus.ocupada,
                onTap: () => setState(() {
                  _filtroStatus = _filtroStatus == MesaStatus.ocupada
                      ? null
                      : MesaStatus.ocupada;
                }),
              ),
              _FiltroChip(
                label: 'Reservada ($resvCount)',
                cor: const Color(0xFF60A5FA),
                active: _filtroStatus == MesaStatus.reservada,
                onTap: () => setState(() {
                  _filtroStatus = _filtroStatus == MesaStatus.reservada
                      ? null
                      : MesaStatus.reservada;
                }),
              ),
            ],
          ),
          const SizedBox(height: 6),
          const Text(
            'Clique em uma mesa para alternar a disponibilidade.',
            style: TextStyle(color: Colors.white54, fontSize: 12),
          ),
          const SizedBox(height: 18),
          GridView.builder(
            shrinkWrap: true,
            physics: const NeverScrollableScrollPhysics(),
            itemCount: mesasVisiveis.length,
            gridDelegate: const SliverGridDelegateWithMaxCrossAxisExtent(
              maxCrossAxisExtent: 140,
              mainAxisExtent: 95,
              crossAxisSpacing: 12,
              mainAxisSpacing: 12,
            ),
            itemBuilder: (context, index) {
              final mesa = mesasVisiveis[index];
              return InkWell(
                borderRadius: BorderRadius.circular(8),
                onTap: () => _alternarStatusMesa(mesa),
                child: Container(
                  padding: const EdgeInsets.all(10),
                  decoration: BoxDecoration(
                    color: _corFundo(mesa.status),
                    borderRadius: BorderRadius.circular(8),
                    border: Border.all(
                      color: _corBorda(mesa.status),
                      width: mesa.status == MesaStatus.disponivel ? 1.5 : 1,
                    ),
                  ),
                  child: Column(
                    mainAxisAlignment: MainAxisAlignment.center,
                    children: [
                      Text(
                        'Mesa ${mesa.numero}',
                        style: const TextStyle(
                          color: Colors.white,
                          fontWeight: FontWeight.bold,
                          fontSize: 14,
                        ),
                      ),
                      const SizedBox(height: 2),
                      Text(
                        '${mesa.capacidade} pess.',
                        style: const TextStyle(
                          color: Colors.white70,
                          fontSize: 11,
                        ),
                      ),
                      const SizedBox(height: 4),
                      Text(
                        mesa.clienteAtual ?? _labelStatus(mesa.status),
                        maxLines: 1,
                        overflow: TextOverflow.ellipsis,
                        style: const TextStyle(
                          color: Colors.white,
                          fontSize: 10,
                          letterSpacing: 0.6,
                        ),
                      ),
                    ],
                  ),
                ),
              );
            },
          ),
        ],
      ),
    );
  }

  Widget _buildFormCard() {
    return Container(
      padding: const EdgeInsets.all(18),
      decoration: BoxDecoration(
        color: const Color(0xFF181818),
        borderRadius: BorderRadius.circular(8),
        border: Border.all(color: Colors.white12),
      ),
      child: Column(
        crossAxisAlignment: CrossAxisAlignment.stretch,
        children: [
          const Text(
            'NOVA ENTRADA NA FILA',
            style: TextStyle(
              color: Colors.white,
              fontSize: 13,
              letterSpacing: 1.5,
              fontWeight: FontWeight.bold,
            ),
          ),
          const SizedBox(height: 14),
          _inputField(_nomeController, 'Nome *', 'Nome do cliente'),
          const SizedBox(height: 10),
          Row(
            children: [
              Expanded(
                flex: 2,
                child: _inputField(
                  _telController,
                  'Telefone',
                  '(45) 00000-0000',
                ),
              ),
              const SizedBox(width: 10),
              Expanded(
                flex: 1,
                child: _inputField(
                  _pessController,
                  'Pessoas *',
                  '1',
                  isNumber: true,
                ),
              ),
            ],
          ),
          const SizedBox(height: 10),
          _inputField(_emailController, 'E-mail', 'cliente@email.com'),
          const SizedBox(height: 10),
          _inputField(
            _obsController,
            'Observações',
            'Alergias, preferências...',
          ),
          const SizedBox(height: 14),
          ElevatedButton(
            onPressed: _adicionarNaFila,
            style: ElevatedButton.styleFrom(
              backgroundColor: Colors.white,
              foregroundColor: Colors.black,
              padding: const EdgeInsets.symmetric(vertical: 14),
              shape: RoundedRectangleBorder(
                borderRadius: BorderRadius.circular(6),
              ),
              elevation: 0,
            ),
            child: const Text(
              'ADICIONAR À FILA',
              style: TextStyle(fontWeight: FontWeight.bold, letterSpacing: 1),
            ),
          ),
        ],
      ),
    );
  }

  Widget _buildFilaCard() {
    return Container(
      padding: const EdgeInsets.all(18),
      decoration: BoxDecoration(
        color: const Color(0xFF181818),
        borderRadius: BorderRadius.circular(8),
        border: Border.all(color: Colors.white12),
      ),
      child: Column(
        crossAxisAlignment: CrossAxisAlignment.stretch,
        children: [
          Row(
            mainAxisAlignment: MainAxisAlignment.spaceBetween,
            children: [
              const Text(
                'FILA DE ESPERA',
                style: TextStyle(
                  color: Colors.white,
                  fontSize: 13,
                  letterSpacing: 1.5,
                  fontWeight: FontWeight.bold,
                ),
              ),
              Text(
                '${_fila.length} na fila',
                style: const TextStyle(color: Colors.white60, fontSize: 12),
              ),
            ],
          ),
          const SizedBox(height: 12),
          if (_fila.isEmpty)
            const Padding(
              padding: EdgeInsets.symmetric(vertical: 12),
              child: Text(
                'Nenhum cliente aguardando.',
                style: TextStyle(color: Colors.white54, fontSize: 13),
              ),
            )
          else
            ..._fila.map(
              (cliente) => Container(
                margin: const EdgeInsets.only(bottom: 10),
                padding: const EdgeInsets.all(12),
                decoration: BoxDecoration(
                  color: const Color(0xFF121212),
                  borderRadius: BorderRadius.circular(6),
                  border: Border.all(
                    color: cliente.chamado ? Colors.white : Colors.white12,
                  ),
                ),
                child: Column(
                  crossAxisAlignment: CrossAxisAlignment.start,
                  children: [
                    Row(
                      mainAxisAlignment: MainAxisAlignment.spaceBetween,
                      children: [
                        Text(
                          '#${cliente.id} - ${cliente.nome}',
                          style: const TextStyle(
                            color: Colors.white,
                            fontWeight: FontWeight.bold,
                            fontSize: 13,
                          ),
                        ),
                        Text(
                          '${cliente.pessoas} pessoas',
                          style: const TextStyle(
                            color: Colors.white70,
                            fontSize: 12,
                          ),
                        ),
                      ],
                    ),
                    const SizedBox(height: 4),
                    Text(
                      'Tel: ${cliente.telefone} | ${cliente.email}',
                      style: const TextStyle(
                        color: Colors.white54,
                        fontSize: 11,
                      ),
                    ),
                    const SizedBox(height: 10),
                    Wrap(
                      spacing: 8,
                      runSpacing: 6,
                      children: [
                        OutlinedButton(
                          style: OutlinedButton.styleFrom(
                            foregroundColor: Colors.white,
                            side: const BorderSide(color: Colors.white38),
                            padding: const EdgeInsets.symmetric(
                              horizontal: 10,
                              vertical: 6,
                            ),
                            minimumSize: Size.zero,
                            tapTargetSize: MaterialTapTargetSize.shrinkWrap,
                          ),
                          onPressed: () {
                            setState(() => cliente.chamado = !cliente.chamado);
                          },
                          child: Text(
                            cliente.chamado ? 'CHAMADO' : 'CHAMAR',
                            style: const TextStyle(fontSize: 11),
                          ),
                        ),
                        ElevatedButton(
                          style: ElevatedButton.styleFrom(
                            backgroundColor: Colors.white,
                            foregroundColor: Colors.black,
                            padding: const EdgeInsets.symmetric(
                              horizontal: 10,
                              vertical: 6,
                            ),
                            minimumSize: Size.zero,
                            tapTargetSize: MaterialTapTargetSize.shrinkWrap,
                            elevation: 0,
                          ),
                          onPressed: () => _atenderCliente(cliente),
                          child: const Text(
                            'ATENDER',
                            style: TextStyle(
                              fontSize: 11,
                              fontWeight: FontWeight.bold,
                            ),
                          ),
                        ),
                        TextButton(
                          style: TextButton.styleFrom(
                            foregroundColor: Colors.white54,
                            padding: const EdgeInsets.symmetric(
                              horizontal: 8,
                              vertical: 6,
                            ),
                            minimumSize: Size.zero,
                            tapTargetSize: MaterialTapTargetSize.shrinkWrap,
                          ),
                          onPressed: () {
                            setState(
                              () =>
                                  _fila.removeWhere((c) => c.id == cliente.id),
                            );
                          },
                          child: const Text(
                            'EXCLUIR',
                            style: TextStyle(fontSize: 11),
                          ),
                        ),
                      ],
                    ),
                  ],
                ),
              ),
            ),
        ],
      ),
    );
  }

  Widget _inputField(
    TextEditingController controller,
    String label,
    String hint, {
    bool isNumber = false,
  }) {
    return TextField(
      controller: controller,
      keyboardType: isNumber ? TextInputType.number : TextInputType.text,
      style: const TextStyle(color: Colors.white, fontSize: 13),
      decoration: InputDecoration(
        labelText: label,
        hintText: hint,
        isDense: true,
        labelStyle: const TextStyle(color: Colors.white60, fontSize: 12),
        hintStyle: const TextStyle(color: Colors.white30, fontSize: 12),
        enabledBorder: OutlineInputBorder(
          borderRadius: BorderRadius.circular(6),
          borderSide: const BorderSide(color: Colors.white24),
        ),
        focusedBorder: OutlineInputBorder(
          borderRadius: BorderRadius.circular(6),
          borderSide: const BorderSide(color: Colors.white),
        ),
      ),
    );
  }
}

class _FiltroChip extends StatelessWidget {
  final String label;
  final Color cor;
  final bool active;
  final VoidCallback onTap;

  const _FiltroChip({
    required this.label,
    required this.cor,
    required this.active,
    required this.onTap,
  });

  @override
  Widget build(BuildContext context) {
    return InkWell(
      borderRadius: BorderRadius.circular(20),
      onTap: onTap,
      child: Container(
        padding: const EdgeInsets.symmetric(horizontal: 12, vertical: 6),
        decoration: BoxDecoration(
          color: active ? cor.withValues(alpha: 0.22) : Colors.transparent,
          borderRadius: BorderRadius.circular(20),
          border: Border.all(color: active ? cor : Colors.white30),
        ),
        child: Row(
          mainAxisSize: MainAxisSize.min,
          children: [
            Container(
              width: 9,
              height: 9,
              decoration: BoxDecoration(color: cor, shape: BoxShape.circle),
            ),
            const SizedBox(width: 7),
            Text(
              label,
              style: TextStyle(
                color: active ? Colors.white : Colors.white70,
                fontSize: 12,
                fontWeight: active ? FontWeight.bold : FontWeight.normal,
              ),
            ),
          ],
        ),
      ),
    );
  }
}
