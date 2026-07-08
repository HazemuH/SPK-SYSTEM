import 'package:equatable/equatable.dart';

import 'criterion.dart';
import 'toy.dart';

/// One toy's normalized value for a criterion in the comparison (`CompareCell`).
class CompareCell extends Equatable {
  const CompareCell({required this.toyId, required this.value, required this.best});

  factory CompareCell.fromJson(Map<String, dynamic> json) {
    return CompareCell(
      toyId: json['toyId']?.toString() ?? '',
      value: (json['value'] as num?)?.toDouble() ?? 0,
      best: json['best'] == true,
    );
  }

  final String toyId;
  final double value;
  final bool best;

  @override
  List<Object?> get props => [toyId, value, best];
}

/// A criterion row in the comparison (`CompareRow`).
class CompareRow extends Equatable {
  const CompareRow({required this.criterion, required this.cells});

  factory CompareRow.fromJson(Map<String, dynamic> json) {
    return CompareRow(
      criterion: Criterion.fromJson(json['criterion'] as Map<String, dynamic>),
      cells: (json['cells'] as List?)
              ?.map((e) => CompareCell.fromJson(e as Map<String, dynamic>))
              .toList() ??
          const [],
    );
  }

  final Criterion criterion;
  final List<CompareCell> cells;

  @override
  List<Object?> get props => [criterion, cells];
}

/// A toy's total SAW score in the comparison (`CompareTotal`).
class CompareTotal extends Equatable {
  const CompareTotal({required this.toyId, required this.score, required this.winner});

  factory CompareTotal.fromJson(Map<String, dynamic> json) {
    return CompareTotal(
      toyId: json['toyId']?.toString() ?? '',
      score: (json['score'] as num?)?.toDouble() ?? 0,
      winner: json['winner'] == true,
    );
  }

  final String toyId;
  final double score;
  final bool winner;

  @override
  List<Object?> get props => [toyId, score, winner];
}

/// Side-by-side comparison of 2–4 toys (`CompareResult`).
class CompareResult extends Equatable {
  const CompareResult({required this.toys, required this.rows, required this.totals});

  factory CompareResult.fromJson(Map<String, dynamic> json) {
    return CompareResult(
      toys: (json['toys'] as List?)
              ?.map((e) => Toy.fromJson(e as Map<String, dynamic>))
              .toList() ??
          const [],
      rows: (json['rows'] as List?)
              ?.map((e) => CompareRow.fromJson(e as Map<String, dynamic>))
              .toList() ??
          const [],
      totals: (json['totals'] as List?)
              ?.map((e) => CompareTotal.fromJson(e as Map<String, dynamic>))
              .toList() ??
          const [],
    );
  }

  final List<Toy> toys;
  final List<CompareRow> rows;
  final List<CompareTotal> totals;

  @override
  List<Object?> get props => [toys, rows, totals];
}
