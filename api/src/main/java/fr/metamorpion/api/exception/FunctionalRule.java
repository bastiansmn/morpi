package fr.metamorpion.api.exception;

import lombok.Getter;

@Getter
public enum FunctionalRule {

    PLAYER_0001("PLAYER_NOT_FOUND", "Joueur introuvable"),
    GAME_0001("GAME_NOT_FOUND", "Partie introuvable"),
    GAME_0002("GAME_FULL", "Partie complète"),
    GAME_0003("ALREADY_IN_GAME", "Vous êtes déjà dans la partie"),
    GAME_0004("NOT_IN_GAME", "Le joueur n'appartient pas à la partie"),
    GAME_0005("MOVE_UNAUTHORIZED", "Impossible de jouer ce coup"),
    ;

    private final String name;
    private final String message;

    FunctionalRule(final String name, final String message) {
        this.name = name;
        this.message = message;
    }

    @Override
    public String toString() {
        return String.format("%s - %s", this.getName(), this.getMessage());
    }

    public String toString(final Object... params) {
        return String.format("%s - %s", this.getName(), String.format(this.getMessage(), params));
    }

}
