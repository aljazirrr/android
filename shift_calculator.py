#!/usr/bin/env python3
"""
Calculator de schimburi pentru muncă în ture.

Utilizare:
    python3 shift_calculator.py

Sau ca modul:
    from shift_calculator import get_shift_on_date
"""

from datetime import date, timedelta


SHIFT_NAMES = {
    1: "Schimbul 1 (dimineata, ~06:00-14:00)",
    2: "Schimbul 2 (dupa-amiaza, ~14:00-22:00)",
    3: "Schimbul 3 / Noapte (~22:00-06:00)",
}

# Ordinea rotatiei: 3 -> 2 -> 1 -> 3 -> ...
ROTATION_ORDER = [3, 2, 1]


def get_week_start(d: date) -> date:
    """Returneaza inceputul saptamanii (luni) pentru o data data."""
    return d - timedelta(days=d.weekday())


def get_shift_on_date(
    reference_date: date,
    reference_shift: int,
    target_date: date,
    rotation: list = None,
    days_per_shift: int = 7,
) -> int:
    """
    Calculeaza schimbul pentru o data tinta, pornind de la o data de referinta.

    Args:
        reference_date: Data de start a saptamanii de referinta (luni)
        reference_shift: Schimbul curent in saptamana de referinta (1, 2 sau 3)
        target_date: Data pentru care vrem sa calculam schimbul
        rotation: Ordinea rotatiei schimburilor (default: [3, 2, 1])
        days_per_shift: Numarul de zile per schimb (default: 7, adica o saptamana)

    Returns:
        Numarul schimbului (1, 2 sau 3)
    """
    if rotation is None:
        rotation = ROTATION_ORDER

    # Calculam startul saptamanii pentru data de referinta
    ref_week_start = get_week_start(reference_date)
    target_week_start = get_week_start(target_date)

    # Numarul de perioade (ex: saptamani) de la referinta la tinta
    days_diff = (target_week_start - ref_week_start).days
    periods_diff = days_diff // days_per_shift

    # Pozitia in rotatia de schimburi
    ref_index = rotation.index(reference_shift)
    target_index = (ref_index + periods_diff) % len(rotation)

    return rotation[target_index]


def main():
    print("=" * 60)
    print("  CALCULATOR DE SCHIMBURI")
    print("=" * 60)

    # --- Parametrii din intrebarea utilizatorului ---
    # "Saptamana asta" = saptamana din 2 martie 2026 (luni)
    # Astazi este 8 martie 2026 (duminica)
    today = date(2026, 3, 8)
    current_week_start = get_week_start(today)  # luni, 2 martie 2026

    current_shift = 3          # schimbul de noapte
    target_date = date(2026, 8, 14)  # 14 august 2026

    print(f"\nData de azi:            {today.strftime('%d %B %Y (%A)')}")
    print(f"Inceputul saptamanii:   {current_week_start.strftime('%d %B %Y (%A)')}")
    print(f"Schimb curent:          {SHIFT_NAMES[current_shift]}")
    print(f"Rotatie schimburi:      {' -> '.join(str(s) for s in ROTATION_ORDER)} -> ...")
    print(f"Zile per schimb:        7 (o saptamana)")
    print(f"\nData tinta:             {target_date.strftime('%d %B %Y (%A)')}")

    # Calcul
    weeks_diff = (get_week_start(target_date) - current_week_start).days // 7
    result_shift = get_shift_on_date(
        reference_date=current_week_start,
        reference_shift=current_shift,
        target_date=target_date,
    )

    print(f"Saptamani pana atunci:  {weeks_diff}")
    print(f"Pozitie in ciclu:       {weeks_diff} % {len(ROTATION_ORDER)} = {weeks_diff % len(ROTATION_ORDER)}")
    print(f"\n{'=' * 60}")
    print(f"  Pe data de 14 august 2026 vei fi in:")
    print(f"  >> {SHIFT_NAMES[result_shift]} <<")
    print(f"{'=' * 60}")

    # Afisam calendarul schimburilor pentru cateva saptamani
    print("\nCalendarul schimburilor (urmatoarele 6 saptamani):")
    print(f"{'Data':20} {'Schimb':10} {'Detalii'}")
    print("-" * 55)
    for i in range(6):
        week_start = current_week_start + timedelta(weeks=i)
        shift = get_shift_on_date(current_week_start, current_shift, week_start)
        marker = " <-- ACUM" if i == 0 else ""
        print(f"{week_start.strftime('%d %b %Y (%a)'):20} S{shift}         {SHIFT_NAMES[shift].split('(')[1].rstrip(')')}{marker}")

    print("\n... (sari peste saptamani) ...")
    # Saptamanile din jurul datei de 14 august
    target_week = get_week_start(target_date)
    for delta in [-1, 0, 1]:
        week = target_week + timedelta(weeks=delta)
        shift = get_shift_on_date(current_week_start, current_shift, week)
        marker = " <-- 14 AUG" if delta == 0 else ""
        print(f"{week.strftime('%d %b %Y (%a)'):20} S{shift}         {SHIFT_NAMES[shift].split('(')[1].rstrip(')')}{marker}")


if __name__ == "__main__":
    main()
